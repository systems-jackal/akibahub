package com.akibahub.controller;

import com.akibahub.dto.request.PayHeroCallbackRequest;
import com.akibahub.model.User;
import com.akibahub.model.Wallet;
import com.akibahub.repository.UserRepository;
import com.akibahub.service.TransactionService;
import com.akibahub.service.WalletService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payhero")
public class PayHeroController {

    private final TransactionService transactionService;
    private final WalletService walletService;
    private final UserRepository userRepository;

    public PayHeroController(TransactionService transactionService,
                             WalletService walletService,
                             UserRepository userRepository) {
        this.transactionService = transactionService;
        this.walletService = walletService;
        this.userRepository = userRepository;
    }

    @PostMapping("/callback")
    public String handleCallback(@RequestBody PayHeroCallbackRequest request) {

        // 1. Ignore failed payments
        if (!"SUCCESS".equalsIgnoreCase(request.getStatus())) {
            return "Ignored";
        }

        // 2. Find user by phone number (important for PayHero)
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Get wallet
        Wallet wallet = walletService.getWalletByUser(user);

        // 4. Process deposit using SAME logic
        transactionService.deposit(
                wallet,
                request.getAmount(),
                request.getReference()
        );

        return "Processed";
    }
}