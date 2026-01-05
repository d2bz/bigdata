package com.sales.service;

import com.sales.utils.JsonUtils;
import com.sales.entity.Product;
import com.sales.repository.ProductRepository;
import com.sales.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DataSyncService {

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisService redisService;

    /**
     * 同步库存数据到HBase
     */
    @Async
    public CompletableFuture<Void> syncStockToHBase(String productId, int delta) {
        try {
            // 获取Redis中的库存
            int redisStock = stockService.getStock(productId);
            
            // 更新HBase中的库存
            productRepository.updateStock(productId, redisStock);
            
            log.info("Stock synced to HBase: productId={}, stock={}", productId, redisStock);
            return CompletableFuture.completedFuture(null);
            
        } catch (IOException e) {
            log.error("Failed to sync stock to HBase: productId={}", productId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 同步商品信息到Redis缓存
     */
    @Async
    public CompletableFuture<Void> syncProductToRedis(Product product) {
        try {
            String cacheKey = "product:cache:" + product.getProductId();
            redisService.set(cacheKey, JsonUtils.toJson(product), 300, TimeUnit.SECONDS); // 5分钟过期
            
            log.info("Product synced to Redis: productId={}", product.getProductId());
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            log.error("Failed to sync product to Redis: productId={}", product.getProductId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 从Redis重建商品缓存
     */
    @Async
    public CompletableFuture<Void> rebuildProductCache(String productId) {
        try {
            // 从HBase获取商品信息
            Product product = productRepository.findById(productId);
            if (product != null) {
                // 同步到Redis
                syncProductToRedis(product);
            }
            
            return CompletableFuture.completedFuture(null);
            
        } catch (IOException e) {
            log.error("Failed to rebuild product cache: productId={}", productId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 定时同步库存数据（每分钟执行）
     */
    @Scheduled(fixedDelay = 60000)
    public void scheduledStockSync() {
        try {
            log.debug("Starting scheduled stock synchronization");
            
            // 获取所有商品
            List<Product> products = productService.getAllProducts(1000);
            
            for (Product product : products) {
                try {
                    int redisStock = stockService.getStock(product.getProductId());
                    int hbaseStock = product.getTotalStock() != null ? product.getTotalStock() : 0;
                    
                    // 如果库存不一致，以Redis为准进行同步
                    if (redisStock != hbaseStock) {
                        productRepository.updateStock(product.getProductId(), redisStock);
                        log.debug("Stock inconsistency fixed: productId={}, redis={}, hbase={}", 
                                product.getProductId(), redisStock, hbaseStock);
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to sync stock for product: {}", product.getProductId(), e);
                }
            }
            
            log.debug("Scheduled stock synchronization completed");
            
        } catch (Exception e) {
            log.error("Scheduled stock synchronization failed", e);
        }
    }

    /**
     * 定时清理过期缓存（每小时执行）
     */
    @Scheduled(fixedDelay = 3600000)
    public void scheduledCacheCleanup() {
        try {
            log.debug("Starting scheduled cache cleanup");
            
            // 清理过期的商品缓存
            List<String> productKeys = new ArrayList<>(redisService.keys("product:cache:*"));
            for (String key : productKeys) {
                try {
                    long ttl = redisService.getExpire(key);
                    if (ttl == -1) { // 没有设置过期时间
                        redisService.expire(key, 300, TimeUnit.SECONDS); // 设置5分钟过期
                    }
                } catch (Exception e) {
                    log.error("Failed to cleanup cache key: {}", key, e);
                }
            }
            
            log.debug("Scheduled cache cleanup completed");
            
        } catch (Exception e) {
            log.error("Scheduled cache cleanup failed", e);
        }
    }

    /**
     * 双写一致性策略 - 更新商品
     */
    public boolean updateProductWithConsistency(Product product) {
        try {
            // 1. 先删除Redis缓存
            String cacheKey = "product:cache:" + product.getProductId();
            redisService.del(cacheKey);
            
            // 2. 更新HBase
            productRepository.save(product);
            
            // 3. 如果更新成功，异步重建缓存
            if (product.getTotalStock() != null) {
                stockService.setStock(product.getProductId(), product.getTotalStock());
            }
            
            // 4. 异步重建商品缓存
            rebuildProductCache(product.getProductId());
            
            log.info("Product updated with consistency: productId={}", product.getProductId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to update product with consistency: productId={}", product.getProductId(), e);
            return false;
        }
    }

    /**
     * 原子性库存更新
     */
    public boolean atomicStockUpdate(String productId, int delta) {
        try {
            // 1. 先更新Redis库存（原子操作）
            long newStock = stockService.decreaseStock(productId, Math.abs(delta));
            
            if (delta > 0) { // 增加库存
                newStock = stockService.increaseStock(productId, delta);
            }
            
            if (newStock < 0) {
                log.warn("Insufficient stock for atomic update: productId={}, delta={}", productId, delta);
                return false;
            }
            
            // 2. 异步更新HBase
            syncStockToHBase(productId, delta);
            
            // 3. 记录库存变更日志
            recordStockChange(productId, delta);
            
            log.info("Atomic stock update completed: productId={}, delta={}, newStock={}", 
                    productId, delta, newStock);
            return true;
            
        } catch (Exception e) {
            log.error("Atomic stock update failed: productId={}, delta={}", productId, delta, e);
            return false;
        }
    }

    /**
     * 记录库存变更日志
     */
    private void recordStockChange(String productId, int delta) {
        try {
            String logKey = "stock:change:log:" + System.currentTimeMillis();
            String logData = JsonUtils.toJson(java.util.Map.of(
                "productId", productId,
                "delta", delta,
                "timestamp", System.currentTimeMillis(),
                "type", delta > 0 ? "increase" : "decrease"
            ));
            
            redisService.set(logKey, logData, 86400, TimeUnit.SECONDS); // 保存24小时
            
        } catch (Exception e) {
            log.error("Failed to record stock change: productId={}, delta={}", productId, delta, e);
        }
    }

    /**
     * 数据一致性检查
     */
    public ConsistencyReport checkDataConsistency() {
        ConsistencyReport report = new ConsistencyReport();
        
        try {
            // 获取所有商品
            List<Product> products = productService.getAllProducts(1000);
            
            int totalChecked = 0;
            int inconsistentCount = 0;
            
            for (Product product : products) {
                totalChecked++;
                
                try {
                    int redisStock = stockService.getStock(product.getProductId());
                    int hbaseStock = product.getTotalStock() != null ? product.getTotalStock() : 0;
                    
                    if (redisStock != hbaseStock) {
                        inconsistentCount++;
                        report.addInconsistentProduct(product.getProductId(), redisStock, hbaseStock);
                        
                        // 自动修复不一致
                        productRepository.updateStock(product.getProductId(), redisStock);
                        log.info("Auto-fixed stock inconsistency: productId={}, redis={}, hbase={}", 
                                product.getProductId(), redisStock, hbaseStock);
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to check consistency for product: {}", product.getProductId(), e);
                }
            }
            
            report.setTotalChecked(totalChecked);
            report.setInconsistentCount(inconsistentCount);
            report.setConsistencyRate(totalChecked > 0 ? (double)(totalChecked - inconsistentCount) / totalChecked : 1.0);
            
            log.info("Data consistency check completed: total={}, inconsistent={}, rate={}", 
                    totalChecked, inconsistentCount, report.getConsistencyRate());
            
        } catch (Exception e) {
            log.error("Data consistency check failed", e);
        }
        
        return report;
    }

    /**
     * 批量同步商品到Redis
     */
    @Async
    public CompletableFuture<Void> batchSyncProductsToRedis(List<String> productIds) {
        try {
            for (String productId : productIds) {
                rebuildProductCache(productId);
            }
            
            log.info("Batch sync products to Redis completed: count={}", productIds.size());
            return CompletableFuture.completedFuture(null);
            
        } catch (Exception e) {
            log.error("Batch sync products to Redis failed", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 一致性报告
     */
    public static class ConsistencyReport {
        private int totalChecked;
        private int inconsistentCount;
        private double consistencyRate;
        private java.util.List<InconsistentItem> inconsistentItems = new java.util.ArrayList<>();

        public void addInconsistentProduct(String productId, int redisStock, int hbaseStock) {
            inconsistentItems.add(new InconsistentItem(productId, redisStock, hbaseStock));
        }

        // Getters and setters
        public int getTotalChecked() { return totalChecked; }
        public void setTotalChecked(int totalChecked) { this.totalChecked = totalChecked; }
        public int getInconsistentCount() { return inconsistentCount; }
        public void setInconsistentCount(int inconsistentCount) { this.inconsistentCount = inconsistentCount; }
        public double getConsistencyRate() { return consistencyRate; }
        public void setConsistencyRate(double consistencyRate) { this.consistencyRate = consistencyRate; }
        public java.util.List<InconsistentItem> getInconsistentItems() { return inconsistentItems; }

        public static class InconsistentItem {
            private String productId;
            private int redisStock;
            private int hbaseStock;

            public InconsistentItem(String productId, int redisStock, int hbaseStock) {
                this.productId = productId;
                this.redisStock = redisStock;
                this.hbaseStock = hbaseStock;
            }

            // Getters
            public String getProductId() { return productId; }
            public int getRedisStock() { return redisStock; }
            public int getHbaseStock() { return hbaseStock; }
        }
    }
}
