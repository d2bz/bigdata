package com.sales.exception;

public class SalesSystemException extends RuntimeException {
    
    private String errorCode;
    
    public SalesSystemException(String message) {
        super(message);
    }
    
    public SalesSystemException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SalesSystemException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public SalesSystemException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
