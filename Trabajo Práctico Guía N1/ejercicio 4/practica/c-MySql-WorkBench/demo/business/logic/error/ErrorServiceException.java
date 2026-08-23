package com.example.demo.business.logic.error;

public class ErrorServiceException extends Exception {
    
    public ErrorServiceException() {}

    public ErrorServiceException(String msg) {
        super(msg);
    }
}

