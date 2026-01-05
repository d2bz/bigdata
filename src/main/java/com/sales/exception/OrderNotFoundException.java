package com.sales.exception;

public class OrderNotFoundException extends SalesSystemException {
    
    public OrderNotFoundException(String orderId) {
        super("ORDER_NOT_FOUND", "Order not found: " + orderId);
    }
    
    public OrderNotFoundException(String orderId, Throwable cause) {
        super("ORDER_NOT_FOUND", "Order not found: " + orderId, cause);
    }
}
