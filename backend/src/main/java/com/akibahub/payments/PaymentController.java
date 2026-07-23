package com.akibahub.payments;

import com.akibahub.idempotency.IdempotencyService;
import com.akibahub.payments.dto.DemoCompleteRequest;
import com.akibahub.payments.dto.PaymentStatusResponse;
import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.shared.exception.BadRequestException;
import com.akibahub.user.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;

    public PaymentController(PaymentService paymentService, IdempotencyService idempotencyService) {
        this.paymentService = paymentService;
        this.idempotencyService = idempotencyService;
    }

    @GetMapping("/status/{reference}")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> status(
            @AuthenticationPrincipal User user,
            @PathVariable String reference) {
        PaymentStatusResponse status = paymentService.getStatus(user, reference);
        return ResponseEntity.ok(ApiResponse.<PaymentStatusResponse>builder()
                .success(true)
                .data(status)
                .build());
    }

    @PostMapping("/demo/complete")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> demoComplete(
            @AuthenticationPrincipal User user,
            @RequestBody DemoCompleteRequest body,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        requireIdempotencyKey(idempotencyKey);
        return idempotencyService.execute(idempotencyKey, user, body,
                new TypeReference<ApiResponse<PaymentStatusResponse>>() {},
                () -> {
                    PaymentStatusResponse result = paymentService.completeDemo(user, body.reference());
                    return ResponseEntity.ok(ApiResponse.<PaymentStatusResponse>builder()
                            .success(true)
                            .message("Deposit completed")
                            .data(result)
                            .build());
                });
    }

    /**
     * Production IPN contract stub. Live signature verification is not
     * enabled until payments.mode=live and PayHero credentials are set.
     * Rejects unverified payloads so clients cannot invent credits.
     */
    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<Void>> callback(@RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.status(501).body(ApiResponse.<Void>builder()
                .success(false)
                .message("PayHero live callback verification is not enabled. "
                        + "Use demo mode (POST /api/payments/demo/complete) for presentations, "
                        + "or configure PAYMENTS_MODE=live with PayHero credentials.")
                .build());
    }

    public static void requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException("Idempotency-Key header is required");
        }
    }
}
