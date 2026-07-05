package com.akibahub.savings.controller;

import com.akibahub.savings.dto.request.DepositRequest;
import com.akibahub.savings.dto.response.TransactionResponse;
import com.akibahub.savings.dto.response.WalletResponse;
import com.akibahub.savings.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/savings")
@RequiredArgsConstructor
@Slf4j
public class SavingsController {

    private final WalletService walletService;

    @Value("${services.internal-key:}")
    private String internalServiceKey;

    @GetMapping("/balance")
    public ResponseEntity<WalletResponse> getBalance(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(
                WalletResponse.from(walletService.getBalance(userId)));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, String>> initiateDeposit(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String userEmail,
            @Valid @RequestBody DepositRequest request) {

        String paymentReference = UUID.randomUUID().toString();

        walletService.initiateDeposit(
                userId, userEmail,
                request.getAmount(),
                request.getPhoneNumber(),
                paymentReference);

        return ResponseEntity.accepted().body(Map.of(
                "message", "Deposit initiated. Complete payment on your phone.",
                "reference", paymentReference,
                "amount", request.getAmount().toString(),
                "currency", "KES"
        ));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestHeader("X-User-Id") String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                walletService.getTransactionHistory(userId, pageable)
                        .map(TransactionResponse::from));
    }

    @PostMapping("/internal/complete-deposit")
    public ResponseEntity<Void> completeDeposit(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestParam String paymentReference,
            @RequestParam BigDecimal amount) {

        if (!validateServiceKey(serviceKey)) {
            log.warn("Rejected internal call — invalid service key");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        walletService.completeDeposit(paymentReference, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/fail-deposit")
    public ResponseEntity<Void> failDeposit(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestParam String paymentReference,
            @RequestParam String reason) {

        if (!validateServiceKey(serviceKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        walletService.failDeposit(paymentReference, reason);
        return ResponseEntity.ok().build();
    }

    private boolean validateServiceKey(String key) {
        return internalServiceKey != null
                && !internalServiceKey.isBlank()
                && internalServiceKey.equals(key);
    }
}
