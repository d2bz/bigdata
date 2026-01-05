package com.sales.controller;

import com.sales.entity.CartItem;
import com.sales.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestBody CartItem cartItem) {
        try {
            cartService.addToCart(cartItem.getUserId(), cartItem);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to add to cart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户购物车
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItem>> getCart(@PathVariable String userId) {
        try {
            List<CartItem> cartItems = cartService.getCart(userId);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            log.error("Failed to get cart: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<Void> updateQuantity(
            @PathVariable String userId,
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        try {
            cartService.updateQuantity(userId, productId, quantity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update cart quantity: userId={}, productId={}, quantity={}", 
                    userId, productId, quantity, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable String userId,
            @PathVariable String productId) {
        try {
            cartService.removeFromCart(userId, productId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to remove from cart: userId={}, productId={}", userId, productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to clear cart: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取购物车商品数量
     */
    @GetMapping("/{userId}/count")
    public ResponseEntity<Integer> getCartItemCount(@PathVariable String userId) {
        try {
            int count = cartService.getCartItemCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Failed to get cart item count: userId={}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 检查购物车中是否存在商品
     */
    @GetMapping("/{userId}/items/{productId}/exists")
    public ResponseEntity<Boolean> existsInCart(
            @PathVariable String userId,
            @PathVariable String productId) {
        try {
            boolean exists = cartService.existsInCart(userId, productId);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            log.error("Failed to check cart item existence: userId={}, productId={}", userId, productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取购物车中商品数量
     */
    @GetMapping("/{userId}/items/{productId}/quantity")
    public ResponseEntity<Integer> getProductQuantity(
            @PathVariable String userId,
            @PathVariable String productId) {
        try {
            int quantity = cartService.getProductQuantity(userId, productId);
            return ResponseEntity.ok(quantity);
        } catch (Exception e) {
            log.error("Failed to get product quantity: userId={}, productId={}", userId, productId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
