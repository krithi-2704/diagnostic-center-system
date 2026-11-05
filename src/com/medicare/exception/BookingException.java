package com.medicare.exception;

public class BookingException extends Exception {
    private static final long serialVersionUID = 1L;
    public BookingException(String message) {
        super(message);
    }
}