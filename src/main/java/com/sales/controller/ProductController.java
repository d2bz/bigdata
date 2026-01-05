package com.sales.controller;

import com.sales.entity.Product;
import com.sales.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 创建商品
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productService.createProduct(product);
            return ResponseEntity.ok(createdProduct);
        } catch (IOException e) {
            log.error("Failed to create product", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable String productId) {
        try {
            Product product = productService.getProductById(productId);
            if (product != null) {
                // 增加浏览量
                productService.incrementViewCount(productId);
                return ResponseEntity.ok(product);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            log.error("Failed to get product: {}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有商品
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Product> products = productService.getAllProducts(limit);
            return ResponseEntity.ok(products);
        } catch (IOException e) {
            log.error("Failed to get all products", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据分类获取商品
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Product> products = productService.getProductsByCategory(category, limit);
            return ResponseEntity.ok(products);
        } catch (IOException e) {
            log.error("Failed to get products by category: {}", category, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据状态获取商品
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Product>> getProductsByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Product> products = productService.getProductsByStatus(status, limit);
            return ResponseEntity.ok(products);
        } catch (IOException e) {
            log.error("Failed to get products by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Product> products = productService.searchProducts(keyword, limit);
            return ResponseEntity.ok(products);
        } catch (IOException e) {
            log.error("Failed to search products: {}", keyword, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新商品信息
     */
    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String productId,
            @RequestBody Product product) {
        try {
            product.setProductId(productId);
            Product updatedProduct = productService.updateProduct(product);
            return ResponseEntity.ok(updatedProduct);
        } catch (IOException e) {
            log.error("Failed to update product: {}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        try {
            productService.deleteProduct(productId);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to delete product: {}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新库存
     */
    @PutMapping("/{productId}/stock")
    public ResponseEntity<Void> updateStock(
            @PathVariable String productId,
            @RequestParam Integer stock) {
        try {
            productService.updateStock(productId, stock);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to update stock: productId={}, stock={}", productId, stock, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 扣减库存
     */
    @PostMapping("/{productId}/stock/deduct")
    public ResponseEntity<Boolean> deductStock(
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        try {
            boolean success = productService.deductStock(productId, quantity);
            return ResponseEntity.ok(success);
        } catch (IOException e) {
            log.error("Failed to deduct stock: productId={}, quantity={}", productId, quantity, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 增加库存
     */
    @PostMapping("/{productId}/stock/increase")
    public ResponseEntity<Void> increaseStock(
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        try {
            productService.increaseStock(productId, quantity);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to increase stock: productId={}, quantity={}", productId, quantity, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取热销商品
     */
    @GetMapping("/hot")
    public ResponseEntity<List<Product>> getHotProducts(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Product> products = productService.getHotProducts(limit);
            return ResponseEntity.ok(products);
        } catch (IOException e) {
            log.error("Failed to get hot products", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取低库存商品
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Product> products = productService.getLowStockProducts(limit);
            return ResponseEntity.ok(products);
        } catch (IOException e) {
            log.error("Failed to get low stock products", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量更新商品状态
     */
    @PutMapping("/batch/status")
    public ResponseEntity<Void> batchUpdateStatus(
            @RequestBody List<String> productIds,
            @RequestParam Integer status) {
        try {
            productService.batchUpdateStatus(productIds, status);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to batch update product status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 检查商品是否存在
     */
    @GetMapping("/{productId}/exists")
    public ResponseEntity<Boolean> checkProductExists(@PathVariable String productId) {
        try {
            boolean exists = productService.productExists(productId);
            return ResponseEntity.ok(exists);
        } catch (IOException e) {
            log.error("Failed to check product existence: {}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 检查商品是否可购买
     */
    @GetMapping("/{productId}/available")
    public ResponseEntity<Boolean> checkProductAvailable(@PathVariable String productId) {
        try {
            boolean available = productService.isProductAvailable(productId);
            return ResponseEntity.ok(available);
        } catch (IOException e) {
            log.error("Failed to check product availability: {}", productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取商品统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ProductService.ProductStats> getProductStats() {
        try {
            ProductService.ProductStats stats = productService.getProductStats();
            return ResponseEntity.ok(stats);
        } catch (IOException e) {
            log.error("Failed to get product stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
