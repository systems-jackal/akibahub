package com.akibahub.shared.exception;

/**
 * The request itself can't be fulfilled as submitted - a bad/missing
 * amount, insufficient balance for a withdrawal, etc. Maps to HTTP 400.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}