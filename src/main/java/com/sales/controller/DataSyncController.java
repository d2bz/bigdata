package com.sales.controller;

import com.sales.service.DataSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/sync")
public class DataSyncController {

    @Autowired
    private DataSyncService dataSyncService;

    /**
     * 手动触发库存同步
     */
    @PostMapping("/stock/{productId}")
    public ResponseEntity<String> syncStock(@PathVariable String productId, @RequestParam int delta) {
        try {
            CompletableFuture<Void> future = dataSyncService.syncStockToHBase(productId, delta);
            future.get(); // 等待完成
            return ResponseEntity.ok("Stock sync initiated");
        } catch (Exception e) {
            log.error("Failed to sync stock: productId={}", productId, e);
            return ResponseEntity.internalServerError().body("Stock sync failed");
        }
    }

    /**
     * 手动触发商品缓存同步
     */
    @PostMapping("/product/{productId}")
    public ResponseEntity<String> syncProduct(@PathVariable String productId) {
        try {
            CompletableFuture<Void> future = dataSyncService.rebuildProductCache(productId);
            future.get(); // 等待完成
            return ResponseEntity.ok("Product cache sync initiated");
        } catch (Exception e) {
            log.error("Failed to sync product: productId={}", productId, e);
            return ResponseEntity.internalServerError().body("Product sync failed");
        }
    }

    /**
     * 批量同步商品缓存
     */
    @PostMapping("/products/batch")
    public ResponseEntity<String> batchSyncProducts(@RequestBody List<String> productIds) {
        try {
            CompletableFuture<Void> future = dataSyncService.batchSyncProductsToRedis(productIds);
            future.get(); // 等待完成
            return ResponseEntity.ok("Batch product sync initiated");
        } catch (Exception e) {
            log.error("Failed to batch sync products", e);
            return ResponseEntity.internalServerError().body("Batch sync failed");
        }
    }

    /**
     * 执行数据一致性检查
     */
    @PostMapping("/consistency/check")
    public ResponseEntity<DataSyncService.ConsistencyReport> checkConsistency() {
        try {
            DataSyncService.ConsistencyReport report = dataSyncService.checkDataConsistency();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Failed to check data consistency", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取同步状态
     */
    @GetMapping("/status")
    public ResponseEntity<String> getSyncStatus() {
        return ResponseEntity.ok("Data sync service is running");
    }
}
