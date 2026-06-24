package com.akibahub.savings.controller;

import com.akibahub.savings.dto.request.DepositRequest;
import com.akibahub.savings.dto.response.TransactionResponse;
import com.akibahub.savings.dto.response.WalletResponse;
import com.akibahub.savings.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final WalletService walletService;

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

    // Internal endpoint — called by payment-service on callback
    @PostMapping("/internal/complete-deposit")
    public ResponseEntity<Void> completeDeposit(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestParam String paymentReference,
            @RequestParam java.math.BigDecimal amount) {
        walletService.completeDeposit(paymentReference, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/fail-deposit")
    public ResponseEntity<Void> failDeposit(
            @RequestHeader("X-Service-Key") String serviceKey,
            @RequestParam String paymentReference,
            @RequestParam String reason) {
        walletService.failDeposit(paymentReference, reason);
        return ResponseEntity.ok().build();
    }
}
