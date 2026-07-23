package com.akibahub.payments;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentControllerIdempotencyTest {

    @Test
    void requireIdempotencyKey_rejectsMissing() {
        assertThrows(com.akibahub.shared.exception.BadRequestException.class,
                () -> PaymentController.requireIdempotencyKey(null));
        assertThrows(com.akibahub.shared.exception.BadRequestException.class,
                () -> PaymentController.requireIdempotencyKey("  "));
    }

    @Test
    void requireIdempotencyKey_acceptsPresent() {
        PaymentController.requireIdempotencyKey("abc-123");
    }
}
