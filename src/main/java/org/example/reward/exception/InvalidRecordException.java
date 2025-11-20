package org.example.reward.exception;


public class InvalidRecordException extends RuntimeException {
    
    public InvalidRecordException(String message) {
        super(message);
    }
    
    public InvalidRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}

