package com.bank.services;

import com.bank.requests.SavingsAccountRequest;

public interface SavingsService {
    void create(SavingsAccountRequest savingsAccountRequest);
    void activateAccount(String savingsId);
    void freezeAccount(String savingsId);
    void closeAccount(String savingsId);
}
