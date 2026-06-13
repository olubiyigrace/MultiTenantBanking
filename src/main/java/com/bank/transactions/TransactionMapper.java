package com.bank.transactions;

import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequest transactionRequest){
       return Transaction.builder()
                .amount(transactionRequest.getAmount())
                .description(transactionRequest.getDescription())
                .transactionType(transactionRequest.getTransactionType())
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction){
        return TransactionResponse.builder()
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .reference(transaction.getReference())
                .transactionType(transaction.getTransactionType())
                .transactionStatus(transaction.getTransactionStatus())
                .build();
    }
}
