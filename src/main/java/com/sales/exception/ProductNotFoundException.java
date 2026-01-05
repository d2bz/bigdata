package com.sales.exception;

public class ProductNotFoundException extends SalesSystemException {
    
    public ProductNotFoundException(String productId) {
        super("PRODUCT_NOT_FOUND", "Product not found: " + productId);
    }
    
    public ProductNotFoundException(String productId, Throwable cause) {
        super("PRODUCT_NOT_FOUND", "Product not found: " + productId, cause);
    }
}
