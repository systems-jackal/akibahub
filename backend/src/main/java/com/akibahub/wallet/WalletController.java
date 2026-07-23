package com.akibahub.wallet;

import com.akibahub.idempotency.IdempotencyService;
import com.akibahub.payments.PaymentController;
import com.akibahub.payments.PaymentService;
import com.akibahub.payments.dto.PaymentStatusResponse;
import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.dto.WalletResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    private final WalletService walletService;
    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;

    public WalletController(WalletService walletService,
                            PaymentService paymentService,
                            IdempotencyService idempotencyService) {
        this.walletService = walletService;
        this.paymentService = paymentService;
        this.idempotencyService = idempotencyService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getMyWallets(@AuthenticationPrincipal User user) {
        List<WalletResponse> wallets = walletService.getUserWallets(user);
        return ResponseEntity.ok(ApiResponse.<List<WalletResponse>>builder()
                .success(true)
                .data(wallets)
                .build());
    }

    /**
     * Initiates an STK-style personal deposit. Does not credit the wallet;
     * returns a PENDING payment reference for status polling / demo complete.
     */
    @PostMapping("/me/personal/deposit")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> depositToPersonal(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        PaymentController.requireIdempotencyKey(idempotencyKey);
        return idempotencyService.execute(idempotencyKey, user, body,
                new TypeReference<ApiResponse<PaymentStatusResponse>>() {},
                () -> {
                    BigDecimal amount = parseAmount(body.get("amount"));
                    String phone = body.get("phone") != null ? String.valueOf(body.get("phone")) : null;
                    PaymentStatusResponse pending = paymentService.initiateDeposit(user, amount, phone);
                    return ResponseEntity.ok(ApiResponse.<PaymentStatusResponse>builder()
                            .success(true)
                            .message("STK Push initiated. Enter your M-Pesa PIN on your phone to complete.")
                            .data(pending)
                            .build());
                });
    }

    @PostMapping("/me/personal/withdraw")
    public ResponseEntity<ApiResponse<WalletResponse>> withdrawFromPersonal(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, BigDecimal> body,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        PaymentController.requireIdempotencyKey(idempotencyKey);
        return idempotencyService.execute(idempotencyKey, user, body,
                new TypeReference<ApiResponse<WalletResponse>>() {},
                () -> {
                    WalletResponse wallet = walletService.withdrawFromPersonal(user, body.get("amount"));
                    return ResponseEntity.ok(ApiResponse.<WalletResponse>builder()
                            .success(true)
                            .message("Withdrawal successful")
                            .data(wallet)
                            .build());
                });
    }

    @PostMapping("/groups/{groupId}/contribute")
    public ResponseEntity<ApiResponse<String>> contributeToGroup(
            @AuthenticationPrincipal User user,
            @PathVariable Long groupId,
            @RequestBody Map<String, BigDecimal> body,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        PaymentController.requireIdempotencyKey(idempotencyKey);
        return idempotencyService.execute(idempotencyKey, user, body,
                new TypeReference<ApiResponse<String>>() {},
                () -> {
                    walletService.contributeToGroup(user, groupId, body.get("amount"));
                    return ResponseEntity.ok(ApiResponse.<String>builder()
                            .success(true)
                            .message("Contribution successful")
                            .build());
                });
    }

    private static BigDecimal parseAmount(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof BigDecimal bd) {
            return bd;
        }
        if (raw instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(raw.toString());
    }
}
