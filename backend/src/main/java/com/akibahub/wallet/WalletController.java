package com.akibahub.wallet;

import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.Wallet;
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

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<Wallet>>> getMyWallets(@AuthenticationPrincipal User user) {
        List<Wallet> wallets = walletService.getUserWallets(user);
        return ResponseEntity.ok(ApiResponse.<List<Wallet>>builder()
                .success(true)
                .data(wallets)
                .build());
    }

    @PostMapping("/me/personal/deposit")
    public ResponseEntity<ApiResponse<Wallet>> depositToPersonal(@AuthenticationPrincipal User user,
                                                                 @RequestBody Map<String, BigDecimal> body) {
        Wallet wallet = walletService.depositToPersonal(user, body.get("amount"));
        return ResponseEntity.ok(ApiResponse.<Wallet>builder()
                .success(true)
                .message("Deposit successful")
                .data(wallet)
                .build());
    }

    @PostMapping("/me/personal/withdraw")
    public ResponseEntity<ApiResponse<Wallet>> withdrawFromPersonal(@AuthenticationPrincipal User user,
                                                                    @RequestBody Map<String, BigDecimal> body) {
        Wallet wallet = walletService.withdrawFromPersonal(user, body.get("amount"));
        return ResponseEntity.ok(ApiResponse.<Wallet>builder()
                .success(true)
                .message("Withdrawal successful")
                .data(wallet)
                .build());
    }

    @PostMapping("/groups/{groupId}/contribute")
    public ResponseEntity<ApiResponse<String>> contributeToGroup(@AuthenticationPrincipal User user,
                                                                 @PathVariable Long groupId,
                                                                 @RequestBody Map<String, BigDecimal> body) {
        walletService.contributeToGroup(user, groupId, body.get("amount"));
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Contribution successful")
                .build());
    }
}