package com.akibahub.service;

import com.akibahub.dto.request.CreateTransactionRequest;
import com.akibahub.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request);

    List<TransactionResponse> getUserTransactions(Long userId);

    List<TransactionResponse> getGroupTransactions(Long groupId);
}