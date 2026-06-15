package com.akibahub.service.impl;

import com.akibahub.dto.request.CreateTransactionRequest;
import com.akibahub.dto.response.TransactionResponse;
import com.akibahub.model.Transaction;
import com.akibahub.model.TransactionType;
import com.akibahub.repository.TransactionRepository;
import com.akibahub.service.TransactionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request) {

        Transaction transaction = Transaction.builder()
                .userId(request.getUserId())
                .groupId(request.getGroupId())
                .amount(request.getAmount())
                .type(TransactionType.valueOf(request.getType()))
                .build();

        Transaction saved = transactionRepository.save(transaction);

        return map(saved);
    }

    @Override
    public List<TransactionResponse> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getGroupTransactions(Long groupId) {
        return transactionRepository.findByGroupId(groupId)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    private TransactionResponse map(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .groupId(t.getGroupId())
                .amount(t.getAmount())
                .type(t.getType())
                .createdAt(t.getCreatedAt())
                .build();
    }
}