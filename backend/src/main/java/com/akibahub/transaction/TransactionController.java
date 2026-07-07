package com.akibahub.transaction;

import com.akibahub.shared.dto.ApiResponse;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.Transaction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<Transaction>>> getMyTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Transaction> transactions = transactionService.getUserTransactions(user, type, groupId, start, end);
        return ResponseEntity.ok(ApiResponse.<List<Transaction>>builder()
                .success(true).data(transactions).build());
    }
}