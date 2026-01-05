package com.sales.service;

import com.sales.config.RedisConfig;
import com.sales.entity.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CartService {

    @Autowired
    private RedisService redisService;

    private static final long CART_EXPIRE_TIME = 7; // 7天

    /**
     * 添加商品到购物车
     */
    public void addToCart(String userId, CartItem cartItem) {
        String cartKey = RedisConfig.RedisKeys.CART_PREFIX + userId;
        
        // 构建购物车项JSON
        String cartItemJson = "{\"quantity\":" + cartItem.getQuantity() + 
                             ",\"add_time\":" + System.currentTimeMillis() / 1000 + "}";
        
        // 添加到Redis Hash
        redisService.hset(cartKey, cartItem.getProductId(), cartItemJson);
        
        // 设置过期时间
        redisService.expire(cartKey, CART_EXPIRE_TIME, TimeUnit.DAYS);
        
        log.info("Added to cart: userId={}, productId={}, quantity={}", 
                userId, cartItem.getProductId(), cartItem.getQuantity());
    }

    /**
     * 获取用户购物车
     */
    public List<CartItem> getCart(String userId) {
        String cartKey = RedisConfig.RedisKeys.CART_PREFIX + userId;
        Map<Object, Object> cartMap = redisService.hgetAll(cartKey);
        
        List<CartItem> cartItems = new ArrayList<>();
        if (cartMap != null) {
            for (Map.Entry<Object, Object> entry : cartMap.entrySet()) {
                String productId = (String) entry.getKey();
                String cartItemJson = (String) entry.getValue();
                
                try {
                    // 这里需要根据实际需求解析JSON并构建CartItem对象
                    CartItem cartItem = CartItem.builder()
                            .userId(userId)
                            .productId(productId)
                            .quantity(1) // 默认值，实际应该从JSON解析
                            .selected(true)
                            .build();
                    cartItems.add(cartItem);
                } catch (Exception e) {
                    log.error("Failed to parse cart item: {}", cartItemJson, e);
                }
            }
        }
        
        return cartItems;
    }

    /**
     * 更新购物车商品数量
     */
    public void updateQuantity(String userId, String productId, Integer quantity) {
        String cartKey = RedisConfig.RedisKeys.CART_PREFIX + userId;
        
        if (quantity <= 0) {
            // 数量小于等于0，删除商品
            redisService.hdel(cartKey, productId);
        } else {
            // 更新数量
            String cartItemJson = "{\"quantity\":" + quantity + 
                                 ",\"add_time\":" + System.currentTimeMillis() / 1000 + "}";
            redisService.hset(cartKey, productId, cartItemJson);
        }
        
        log.info("Updated cart quantity: userId={}, productId={}, quantity={}", 
                userId, productId, quantity);
    }

    /**
     * 删除购物车商品
     */
    public void removeFromCart(String userId, String productId) {
        String cartKey = RedisConfig.RedisKeys.CART_PREFIX + userId;
        redisService.hdel(cartKey, productId);
        
        log.info("Removed from cart: userId={}, productId={}", userId, productId);
    }

    /**
     * 清空购物车
     */
    public void clearCart(String userId) {
        String cartKey = RedisConfig.RedisKeys.CART_PREFIX + userId;
        redisService.del(cartKey);
        
        log.info("Cleared cart: userId={}", userId);
    }

    /**
     * 获取购物车商品数量
     */
    public int getCartItemCount(String userId) {
        String cartKey = RedisConfig.RedisKeys.CART_PREFIX + userId;
        Map<Object, Object> cartMap = redisService.hgetAll(cartKey);
        
        if (cartMap == null) {
            return 0;
        }
        
        int totalCount = 0;
        for (Object value : cartMap.values()) {
            try {
                String cartItemJson = (String) value;
                // 简单解析数量
                if (cartItemJson.contains("\"quantity\":")) {
                    int start = cartItemJson.indexOf("\"quantity\":") + 12;
                    int end = cartItemJson.indexOf(",", start);
                    if (end == -1) {
                        end = cartItemJson.indexOf("}", start);
                    }
                    if (end > start) {
                        String quantityStr = cartItemJson.substring(start, end);
                        totalCount += Integer.parseInt(quantityStr.trim());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse cart item quantity: {}", value, e);
            }
        }
        
        return totalCount;
    }

    /**
     * 检查购物车中是否存在商品
     */
    public boolean existsInCart(String userId, String productId) {
        String cartKey = RedisConfig.RedisKeys.CART_PREFIX + userId;
        return redisService.hexists(cartKey, productId);
    }

    /**
     * 获取购物车中商品数量
     */
    public int getProductQuantity(String userId, String productId) {
        String cartKey = RedisConfig.RedisKeys.CART_PREFIX + userId;
        String cartItemJson = (String) redisService.hget(cartKey, productId);
        
        if (cartItemJson == null) {
            return 0;
        }
        
        try {
            // 简单解析数量
            if (cartItemJson.contains("\"quantity\":")) {
                int start = cartItemJson.indexOf("\"quantity\":") + 12;
                int end = cartItemJson.indexOf(",", start);
                if (end == -1) {
                    end = cartItemJson.indexOf("}", start);
                }
                if (end > start) {
                    String quantityStr = cartItemJson.substring(start, end);
                    return Integer.parseInt(quantityStr.trim());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse cart item quantity: {}", cartItemJson, e);
        }
        
        return 0;
    }
}
