package com.bank.services;


import com.bank.dtos.requestDtos.DepositRequest;
import jakarta.mail.MessagingException;

public interface TransactionService {
    void createDeposit(DepositRequest depositRequest);
}
