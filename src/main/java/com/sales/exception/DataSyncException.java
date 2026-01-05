package com.sales.exception;

public class DataSyncException extends SalesSystemException {
    
    public DataSyncException(String message) {
        super("DATA_SYNC_ERROR", message);
    }
    
    public DataSyncException(String message, Throwable cause) {
        super("DATA_SYNC_ERROR", message, cause);
    }
}
