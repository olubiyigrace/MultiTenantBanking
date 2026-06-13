package com.bank.transactions;


import jakarta.mail.MessagingException;

public interface TransactionService {
    void createDeposit(TransactionRequest transactionRequest) throws MessagingException;
}
