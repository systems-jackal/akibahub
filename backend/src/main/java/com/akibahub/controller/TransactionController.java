package com.akibahub.controller;

import com.akibahub.dto.request.DepositRequest;
import com.akibahub.model.User;
import com.akibahub.model.Wallet;
import com.akibahub.repository.UserRepository;
import com.akibahub.service.TransactionService;
import com.akibahub.service.WalletService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final WalletService walletService;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService,
                                 WalletService walletService,
                                 UserRepository userRepository) {
        this.transactionService = transactionService;
        this.walletService = walletService;
        this.userRepository = userRepository;
    }

    @PostMapping("/deposit")
    public String deposit(@RequestBody DepositRequest request) {

        // 1. Find user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Get wallet
        Wallet wallet = walletService.getWalletByUser(user);

        // 3. Process deposit
        transactionService.deposit(
                wallet,
                request.getAmount(),
                request.getReference()
        );

        return "Deposit successful";
    }
}