package com.smartbudgeter.demo.exception;


// Duplicate budget exception
public class DuplicateBudgetException extends RuntimeException {
    
    public DuplicateBudgetException(String message) {
        super(message);
    }
    
    public DuplicateBudgetException(String message, Throwable cause) {
        super(message, cause);
    }}