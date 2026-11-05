package com.medicare.exception;

public class BranchUnauthorizedException extends Exception {
    private static final long serialVersionUID = 1L;
    public BranchUnauthorizedException(String message) {
        super(message);
    }
}