package com.sales.exception;

public class UserNotFoundException extends SalesSystemException {
    
    public UserNotFoundException(String userId) {
        super("USER_NOT_FOUND", "User not found: " + userId);
    }
    
    public UserNotFoundException(String userId, Throwable cause) {
        super("USER_NOT_FOUND", "User not found: " + userId, cause);
    }
}
