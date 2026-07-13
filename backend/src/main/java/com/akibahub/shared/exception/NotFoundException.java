package com.akibahub.shared.exception;

/**
 * The requested resource doesn't exist (or, for security purposes,
 * should be treated as if it doesn't - see ForbiddenException for the
 * "exists but you can't have it" case). Maps to HTTP 404.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}