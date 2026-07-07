package com.akibahub.wallet;

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
    public ResponseEntity<List<Wallet>> getMyWallets(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(walletService.getUserWallets(user));
    }

    @PostMapping("/me/personal/deposit")
    public ResponseEntity<Wallet> depositToPersonal(@AuthenticationPrincipal User user,
                                                    @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(walletService.depositToPersonal(user, body.get("amount")));
    }

    @PostMapping("/groups/{groupId}/contribute")
    public ResponseEntity<?> contributeToGroup(@AuthenticationPrincipal User user,
                                               @PathVariable Long groupId,
                                               @RequestBody Map<String, BigDecimal> body) {
        walletService.contributeToGroup(user, groupId, body.get("amount"));
        return ResponseEntity.ok(Map.of("message", "Contribution successful"));
    }
}