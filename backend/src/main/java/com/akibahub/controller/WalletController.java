package com.akibahub.controller;

import com.akibahub.model.User;
import com.akibahub.model.Wallet;
import com.akibahub.repository.UserRepository;
import com.akibahub.service.WalletService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "*")
public class WalletController {

    private final WalletService walletService;
    private final UserRepository userRepository;

    public WalletController(WalletService walletService,
                            UserRepository userRepository) {
        this.walletService = walletService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}")
    public Wallet getWallet(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return walletService.getWalletByUser(user);
    }
}