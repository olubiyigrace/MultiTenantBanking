package com.bank.services;

import com.bank.requests.SavingsAccountRequest;
import com.bank.responses.TotalInterestCollectedResponse;
import com.bank.responses.TotalLoansOutstandingResponse;
import com.bank.responses.TotalLoansOverdueResponse;
import com.bank.responses.TotalSavingsResponse;

import java.time.Month;
import java.time.Year;

public interface SavingsService {
    void create(SavingsAccountRequest savingsAccountRequest);
    void activateAccount(String savingsId);
    void freezeAccount(String savingsId);
    void closeAccount(String savingsId);
    TotalSavingsResponse getTotalSavings();
    TotalLoansOutstandingResponse getTotalLoansOutstanding();
    TotalLoansOverdueResponse getTotalLoansOverdue();
    TotalInterestCollectedResponse getTotalInterestCollected(Month month, Year year);
}
