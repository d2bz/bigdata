package com.sales.exception;

public class InsufficientStockException extends SalesSystemException {
    
    private String productId;
    private int requestedQuantity;
    private int availableQuantity;
    
    public InsufficientStockException(String productId, int requestedQuantity, int availableQuantity) {
        super("INSUFFICIENT_STOCK", 
              String.format("Insufficient stock for product %s: requested=%d, available=%d", 
                           productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public int getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
