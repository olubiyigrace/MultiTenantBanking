package com.bank.services;

import com.bank.requests.SavingsAccountRequest;
import com.bank.responses.TotalLoansOutstandingResponse;
import com.bank.responses.TotalSavingsResponse;

public interface SavingsService {
    void create(SavingsAccountRequest savingsAccountRequest);
    void activateAccount(String savingsId);
    void freezeAccount(String savingsId);
    void closeAccount(String savingsId);
    TotalSavingsResponse getTotalSavings();
    TotalLoansOutstandingResponse getTotalLoansOutstanding();
}
