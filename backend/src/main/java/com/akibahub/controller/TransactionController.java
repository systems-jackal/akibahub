package com.akibahub.controller;

import com.akibahub.dto.request.CreateTransactionRequest;
import com.akibahub.dto.response.TransactionResponse;
import com.akibahub.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    public TransactionResponse create(@RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(request);
    }

    @GetMapping("/user/{userId}")
    public List<TransactionResponse> getUser(@PathVariable Long userId) {
        return transactionService.getUserTransactions(userId);
    }

    @GetMapping("/group/{groupId}")
    public List<TransactionResponse> getGroup(@PathVariable Long groupId) {
        return transactionService.getGroupTransactions(groupId);
    }
}