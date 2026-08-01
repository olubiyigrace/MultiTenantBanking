package com.bank.mappers;

import com.bank.entities.Transaction;
import com.bank.dtos.requestDtos.DepositRequest;
import com.bank.dtos.responseDtos.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(DepositRequest depositRequest){
       return Transaction.builder()
                .amount(depositRequest.getAmount())
                .description(depositRequest.getDescription())
                .transactionType(depositRequest.getTransactionType())
                .depositSlipNumber(depositRequest.getDepositSlipNumber())
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
