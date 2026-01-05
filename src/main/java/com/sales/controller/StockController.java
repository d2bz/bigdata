package com.sales.controller;

import com.sales.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    /**
     * 设置商品库存
     */
    @PostMapping("/set")
    public ResponseEntity<Void> setStock(
            @RequestParam String productId,
            @RequestParam int stock) {
        try {
            stockService.setStock(productId, stock);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to set stock: productId={}, stock={}", productId, stock, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品库存
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Integer> getStock(@PathVariable String productId) {
        try {
            int stock = stockService.getStock(productId);
            return ResponseEntity.ok(stock);
        } catch (Exception e) {
            log.error("Failed to get stock: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 增加库存
     */
    @PostMapping("/{productId}/increase")
    public ResponseEntity<Long> increaseStock(
            @PathVariable String productId,
            @RequestParam int delta) {
        try {
            long newStock = stockService.increaseStock(productId, delta);
            return ResponseEntity.ok(newStock);
        } catch (Exception e) {
            log.error("Failed to increase stock: productId={}, delta={}", productId, delta, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 减少库存
     */
    @PostMapping("/{productId}/decrease")
    public ResponseEntity<Long> decreaseStock(
            @PathVariable String productId,
            @RequestParam int delta) {
        try {
            long newStock = stockService.decreaseStock(productId, delta);
            if (newStock >= 0) {
                return ResponseEntity.ok(newStock);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("Failed to decrease stock: productId={}, delta={}", productId, delta, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 原子性扣减库存
     */
    @PostMapping("/{productId}/deduct")
    public ResponseEntity<Boolean> deductStock(
            @PathVariable String productId,
            @RequestParam int quantity) {
        try {
            boolean success = stockService.deductStock(productId, quantity);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            log.error("Failed to deduct stock: productId={}, quantity={}", productId, quantity, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 设置秒杀库存
     */
    @PostMapping("/seckill/set")
    public ResponseEntity<Void> setSeckillStock(
            @RequestParam String seckillId,
            @RequestParam String productId,
            @RequestParam int stock) {
        try {
            stockService.setSeckillStock(seckillId, productId, stock);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to set seckill stock: seckillId={}, productId={}, stock={}", 
                    seckillId, productId, stock, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取秒杀库存
     */
    @GetMapping("/seckill/{seckillId}/{productId}")
    public ResponseEntity<Integer> getSeckillStock(
            @PathVariable String seckillId,
            @PathVariable String productId) {
        try {
            int stock = stockService.getSeckillStock(seckillId, productId);
            return ResponseEntity.ok(stock);
        } catch (Exception e) {
            log.error("Failed to get seckill stock: seckillId={}, productId={}", seckillId, productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 扣减秒杀库存
     */
    @PostMapping("/seckill/{seckillId}/{productId}/deduct")
    public ResponseEntity<Boolean> deductSeckillStock(
            @PathVariable String seckillId,
            @PathVariable String productId,
            @RequestParam int quantity) {
        try {
            boolean success = stockService.deductSeckillStock(seckillId, productId, quantity);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            log.error("Failed to deduct seckill stock: seckillId={}, productId={}, quantity={}", 
                    seckillId, productId, quantity, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 检查库存是否存在
     */
    @GetMapping("/{productId}/exists")
    public ResponseEntity<Boolean> stockExists(@PathVariable String productId) {
        try {
            boolean exists = stockService.stockExists(productId);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            log.error("Failed to check stock existence: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除库存
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteStock(@PathVariable String productId) {
        try {
            stockService.deleteStock(productId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete stock: productId={}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量获取库存
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Integer>> batchGetStock(@RequestBody List<String> productIds) {
        try {
            List<Integer> stocks = stockService.batchGetStock(productIds);
            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            log.error("Failed to batch get stock", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 预占库存
     */
    @PostMapping("/{productId}/lock")
    public ResponseEntity<Boolean> lockStock(
            @PathVariable String productId,
            @RequestParam int quantity) {
        try {
            boolean success = stockService.lockStock(productId, quantity);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            log.error("Failed to lock stock: productId={}, quantity={}", productId, quantity, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 释放预占库存
     */
    @PostMapping("/{productId}/release")
    public ResponseEntity<Void> releaseStock(
            @PathVariable String productId,
            @RequestParam int quantity) {
        try {
            stockService.releaseStock(productId, quantity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to release stock: productId={}, quantity={}", productId, quantity, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
