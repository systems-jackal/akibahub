package com.akibahub.shared.exception;

/**
 * The request is well-formed and the caller is allowed to make it, but it
 * conflicts with the current state of the resource - already a member,
 * already voted, voting already closed, trying to delete a group that
 * still holds a balance. Maps to HTTP 409.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}