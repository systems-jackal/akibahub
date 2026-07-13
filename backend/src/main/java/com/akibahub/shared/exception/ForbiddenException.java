package com.akibahub.shared.exception;

/**
 * The resource exists, but the authenticated caller isn't allowed to see
 * or act on it - not a member of the group, not the creator of the
 * proposal, etc. Maps to HTTP 403.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}