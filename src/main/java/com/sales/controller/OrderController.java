package com.sales.controller;

import com.sales.entity.Order;
import com.sales.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        try {
            Order createdOrder = orderService.createOrder(order);
            return ResponseEntity.ok(createdOrder);
        } catch (IOException e) {
            log.error("Failed to create order", e);
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            log.error("Failed to create order: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order != null) {
                return ResponseEntity.ok(order);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            log.error("Failed to get order: {}", orderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(
            @PathVariable String userId,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Order> orders = orderService.getUserOrders(userId, limit);
            return ResponseEntity.ok(orders);
        } catch (IOException e) {
            log.error("Failed to get user orders: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据状态获取订单列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Order> orders = orderService.getOrdersByStatus(status, limit);
            return ResponseEntity.ok(orders);
        } catch (IOException e) {
            log.error("Failed to get orders by status: {}", status, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取最近订单
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Order>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Order> orders = orderService.getRecentOrders(limit);
            return ResponseEntity.ok(orders);
        } catch (IOException e) {
            log.error("Failed to get recent orders", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Boolean> payOrder(
            @PathVariable String orderId,
            @RequestParam String payMethod) {
        try {
            boolean success = orderService.payOrder(orderId, payMethod);
            if (success) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.badRequest().body(false);
            }
        } catch (IOException e) {
            log.error("Failed to pay order: {}", orderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 发货
     */
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<Boolean> deliverOrder(
            @PathVariable String orderId,
            @RequestParam String expressCompany,
            @RequestParam String expressNo) {
        try {
            boolean success = orderService.deliverOrder(orderId, expressCompany, expressNo);
            if (success) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.badRequest().body(false);
            }
        } catch (IOException e) {
            log.error("Failed to deliver order: {}", orderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<Boolean> completeOrder(@PathVariable String orderId) {
        try {
            boolean success = orderService.completeOrder(orderId);
            if (success) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.badRequest().body(false);
            }
        } catch (IOException e) {
            log.error("Failed to complete order: {}", orderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Boolean> cancelOrder(@PathVariable String orderId) {
        try {
            boolean success = orderService.cancelOrder(orderId);
            if (success) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.badRequest().body(false);
            }
        } catch (IOException e) {
            log.error("Failed to cancel order: {}", orderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam Integer status) {
        try {
            orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to update order status: {}", orderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新物流信息
     */
    @PutMapping("/{orderId}/logistics")
    public ResponseEntity<Void> updateLogistics(
            @PathVariable String orderId,
            @RequestParam String expressCompany,
            @RequestParam String expressNo) {
        try {
            orderService.updateLogistics(orderId, expressCompany, expressNo);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Failed to update logistics: {}", orderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 检查订单是否存在
     */
    @GetMapping("/{orderId}/exists")
    public ResponseEntity<Boolean> checkOrderExists(@PathVariable String orderId) {
        try {
            boolean exists = orderService.orderExists(orderId);
            return ResponseEntity.ok(exists);
        } catch (IOException e) {
            log.error("Failed to check order existence: {}", orderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<OrderService.OrderStats> getOrderStats() {
        try {
            OrderService.OrderStats stats = orderService.getOrderStats();
            return ResponseEntity.ok(stats);
        } catch (IOException e) {
            log.error("Failed to get order stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
