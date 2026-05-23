package com.bank.services;

import com.bank.requests.SavingsAccountRequest;

public interface SavingsService {
    void create(SavingsAccountRequest savingsAccountRequest);
}
