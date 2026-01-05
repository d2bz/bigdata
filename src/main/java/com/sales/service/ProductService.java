package com.sales.service;

import com.sales.entity.Product;
import com.sales.repository.ProductRepository;
import com.sales.service.RankingService;
import com.sales.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private RankingService rankingService;

    /**
     * 创建商品
     */
    public Product createProduct(Product product) throws IOException {
        // 生成商品ID
        if (product.getProductId() == null || product.getProductId().isEmpty()) {
            product.setProductId(generateProductId());
        }

        // 设置默认值
        if (product.getCreateTime() == null) {
            product.setCreateTime(LocalDateTime.now());
        }
        if (product.getUpdateTime() == null) {
            product.setUpdateTime(LocalDateTime.now());
        }
        if (product.getStatus() == null) {
            product.setStatus(Product.Status.ON_SHELF.getCode());
        }
        if (product.getViewCount() == null) {
            product.setViewCount(0L);
        }
        if (product.getSaleCount() == null) {
            product.setSaleCount(0L);
        }
        if (product.getCollectCount() == null) {
            product.setCollectCount(0L);
        }
        if (product.getLockStock() == null) {
            product.setLockStock(0);
        }

        // 保存到HBase
        productRepository.save(product);

        // 初始化Redis库存
        if (product.getTotalStock() != null) {
            stockService.setStock(product.getProductId(), product.getTotalStock());
        }

        log.info("Product created: {}", product.getProductId());
        return product;
    }

    /**
     * 根据ID获取商品
     */
    @Cacheable(value = "product", key = "#productId")
    public Product getProductById(String productId) throws IOException {
        Product product = productRepository.findById(productId);
        
        if (product != null) {
            // 获取实时库存
            product.setRealTimeStock(stockService.getStock(productId));
        }
        
        return product;
    }

    /**
     * 更新商品信息
     */
    @CachePut(value = "product", key = "#product.productId")
    public Product updateProduct(Product product) throws IOException {
        product.setUpdateTime(LocalDateTime.now());
        
        // 更新HBase
        productRepository.save(product);
        
        // 如果库存发生变化，同步到Redis
        if (product.getTotalStock() != null) {
            stockService.setStock(product.getProductId(), product.getTotalStock());
        }
        
        log.info("Product updated: {}", product.getProductId());
        return product;
    }

    /**
     * 删除商品
     */
    @CacheEvict(value = "product", key = "#productId")
    public void deleteProduct(String productId) throws IOException {
        productRepository.deleteById(productId);
        stockService.deleteStock(productId);
        
        log.info("Product deleted: {}", productId);
    }

    /**
     * 获取所有商品
     */
    public List<Product> getAllProducts(int limit) throws IOException {
        List<Product> products = productRepository.findAll(limit);
        
        // 设置实时库存
        for (Product product : products) {
            product.setRealTimeStock(stockService.getStock(product.getProductId()));
        }
        
        return products;
    }

    /**
     * 根据分类获取商品
     */
    public List<Product> getProductsByCategory(String category, int limit) throws IOException {
        List<Product> products = productRepository.findByCategory(category, limit);
        
        // 设置实时库存
        for (Product product : products) {
            product.setRealTimeStock(stockService.getStock(product.getProductId()));
        }
        
        return products;
    }

    /**
     * 根据状态获取商品
     */
    public List<Product> getProductsByStatus(Integer status, int limit) throws IOException {
        List<Product> products = productRepository.findByStatus(status, limit);
        
        // 设置实时库存
        for (Product product : products) {
            product.setRealTimeStock(stockService.getStock(product.getProductId()));
        }
        
        return products;
    }

    /**
     * 搜索商品
     */
    public List<Product> searchProducts(String keyword, int limit) throws IOException {
        List<Product> products = productRepository.findByNameContaining(keyword, limit);
        
        // 设置实时库存
        for (Product product : products) {
            product.setRealTimeStock(stockService.getStock(product.getProductId()));
        }
        
        return products;
    }

    /**
     * 更新库存
     */
    public void updateStock(String productId, Integer stock) throws IOException {
        productRepository.updateStock(productId, stock);
        stockService.setStock(productId, stock);
        
        log.info("Stock updated: productId={}, stock={}", productId, stock);
    }

    /**
     * 扣减库存
     */
    public boolean deductStock(String productId, Integer quantity) throws IOException {
        // 先从Redis扣减
        boolean success = stockService.deductStock(productId, quantity);
        
        if (success) {
            // 异步更新HBase库存
            try {
                Product product = productRepository.findById(productId);
                if (product != null && product.getTotalStock() != null) {
                    int newStock = product.getTotalStock() - quantity;
                    productRepository.updateStock(productId, newStock);
                }
            } catch (IOException e) {
                log.error("Failed to update HBase stock: productId={}", productId, e);
                // 这里可以加入重试机制或消息队列
            }
        }
        
        return success;
    }

    /**
     * 增加库存
     */
    public void increaseStock(String productId, Integer quantity) throws IOException {
        stockService.increaseStock(productId, quantity);
        
        // 异步更新HBase库存
        try {
            Product product = productRepository.findById(productId);
            if (product != null && product.getTotalStock() != null) {
                int newStock = product.getTotalStock() + quantity;
                productRepository.updateStock(productId, newStock);
            }
        } catch (IOException e) {
            log.error("Failed to update HBase stock: productId={}", productId, e);
        }
        
        log.info("Stock increased: productId={}, quantity={}", productId, quantity);
    }

    /**
     * 增加浏览量
     */
    public void incrementViewCount(String productId) throws IOException {
        productRepository.incrementViewCount(productId);
        rankingService.addViewScore(productId);
        
        log.debug("View count incremented: productId={}", productId);
    }

    /**
     * 增加销量
     */
    public void incrementSaleCount(String productId, Long quantity) throws IOException {
        productRepository.incrementSaleCount(productId, quantity);
        rankingService.addPurchaseScore(productId, quantity);
        
        log.info("Sale count incremented: productId={}, quantity={}", productId, quantity);
    }

    /**
     * 检查商品是否存在
     */
    public boolean productExists(String productId) throws IOException {
        return productRepository.existsById(productId);
    }

    /**
     * 检查商品是否可购买
     */
    public boolean isProductAvailable(String productId) throws IOException {
        Product product = getProductById(productId);
        return product != null && product.isAvailable();
    }

    /**
     * 获取热销商品
     */
    public List<Product> getHotProducts(int limit) throws IOException {
        // 从Redis获取热销商品ID列表
        List<String> hotProductIds = rankingService.getHotProducts(limit)
                .stream()
                .map(Object::toString)
                .toList();

        // 批量获取商品信息
        List<Product> hotProducts = new java.util.ArrayList<>();
        for (String productId : hotProductIds) {
            try {
                Product product = getProductById(productId);
                if (product != null) {
                    hotProducts.add(product);
                }
            } catch (IOException e) {
                log.error("Failed to get hot product: productId={}", productId, e);
            }
        }

        return hotProducts;
    }

    /**
     * 获取低库存商品
     */
    public List<Product> getLowStockProducts(int limit) throws IOException {
        List<Product> allProducts = getAllProducts(limit * 2); // 获取更多商品进行筛选
        
        return allProducts.stream()
                .filter(product -> {
                    Integer stock = product.getRealTimeStock();
                    Integer safeStock = product.getSafeStock();
                    return stock != null && safeStock != null && stock <= safeStock;
                })
                .limit(limit)
                .toList();
    }

    /**
     * 批量更新商品状态
     */
    public void batchUpdateStatus(List<String> productIds, Integer status) throws IOException {
        for (String productId : productIds) {
            try {
                Product product = productRepository.findById(productId);
                if (product != null) {
                    product.setStatus(status);
                    product.setUpdateTime(LocalDateTime.now());
                    productRepository.save(product);
                }
            } catch (IOException e) {
                log.error("Failed to update product status: productId={}, status={}", productId, status, e);
            }
        }
        
        log.info("Batch updated product status: count={}, status={}", productIds.size(), status);
    }

    /**
     * 生成商品ID
     */
    private String generateProductId() {
        // 简单的ID生成策略，实际项目中可以使用更复杂的算法
        return "P" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 获取商品统计信息
     */
    public ProductStats getProductStats() throws IOException {
        List<Product> allProducts = getAllProducts(10000); // 假设最多10000个商品
        
        int totalCount = allProducts.size();
        long onShelfCount = allProducts.stream()
                .filter(p -> Product.Status.ON_SHELF.getCode().equals(p.getStatus()))
                .count();
        long lowStockCount = allProducts.stream()
                .filter(p -> {
                    Integer stock = p.getRealTimeStock();
                    Integer safeStock = p.getSafeStock();
                    return stock != null && safeStock != null && stock <= safeStock;
                })
                .count();
        
        return ProductStats.builder()
                .totalCount(totalCount)
                .onShelfCount((int) onShelfCount)
                .lowStockCount((int) lowStockCount)
                .build();
    }

    /**
     * 商品统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class ProductStats {
        private int totalCount;
        private int onShelfCount;
        private int lowStockCount;
    }
}
