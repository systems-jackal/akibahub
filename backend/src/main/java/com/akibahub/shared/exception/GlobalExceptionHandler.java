package com.akibahub.shared.exception;

import com.akibahub.shared.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Spring picks the MOST SPECIFIC matching @ExceptionHandler for a thrown
 * exception, regardless of the order these methods appear in this file -
 * so registering a handler for e.g. NullPointerException here means it
 * will be used instead of the generic RuntimeException handler below,
 * even though NullPointerException IS a RuntimeException.
 *
 * The rule this file follows: anything we threw ourselves, on purpose,
 * with a message written for the end user, is safe to send back as-is.
 * Anything we did NOT throw on purpose (NPEs, DB constraint violations,
 * literally anything else) gets logged in full on the server, and the
 * client only ever sees a generic message plus a reference id they can
 * quote to support - never the raw exception detail.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- our own typed business exceptions: safe to expose ----------

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.<Void>builder().success(false).message(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.<Void>builder().success(false).message(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.<Void>builder().success(false).message(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<Void>builder().success(false).message(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<Map<String, String>>builder()
                        .success(false).message("Validation failed").data(fieldErrors).build()
        );
    }

    // ---------- unexpected failures: log details, never leak them ----------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String refId = logUnexpected(ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.<Void>builder().success(false)
                        .message("This action conflicts with existing data. Reference: " + refId).build()
        );
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointer(NullPointerException ex) {
        String refId = logUnexpected(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<Void>builder().success(false)
                        .message("Something went wrong on our end. Reference: " + refId).build()
        );
    }

    // ---------- legacy fallback ----------
    // Older business logic in this codebase throws a plain RuntimeException
    // with a hand-written message (e.g. "Insufficient balance") instead of
    // one of the typed exceptions above. Those messages ARE safe to show -
    // a developer wrote them for the user - so this stays a 400 rather than
    // getting swept into the generic handler below. As each service is
    // migrated to throw NotFoundException/ForbiddenException/etc instead,
    // this handler is reached less and less; it should eventually be
    // removable entirely.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<Void>builder().success(false).message(ex.getMessage()).build()
        );
    }

    // True catch-all: anything not covered above (checked exceptions,
    // Errors that aren't fatal, anything we didn't anticipate).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        String refId = logUnexpected(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<Void>builder().success(false)
                        .message("An unexpected error occurred. Reference: " + refId).build()
        );
    }

    private String logUnexpected(Exception ex) {
        String refId = UUID.randomUUID().toString().substring(0, 8);
        log.error("Unhandled exception [ref={}]", refId, ex);
        return refId;
    }
}