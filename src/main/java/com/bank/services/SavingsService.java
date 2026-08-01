package com.bank.services;

import com.bank.dtos.requestDtos.SavingsAccountRequest;
import com.bank.dtos.responseDtos.TotalInterestCollectedResponse;
import com.bank.dtos.responseDtos.TotalLoansOutstandingResponse;
import com.bank.dtos.responseDtos.TotalLoansOverdueResponse;
import com.bank.dtos.responseDtos.TotalSavingsResponse;

import java.time.Month;
import java.time.Year;

public interface SavingsService {
    void createAnotherSavingsAccount(SavingsAccountRequest savingsAccountRequest);
    void activateAccount(String savingsId);
    void freezeAccount(String savingsId);
    void closeAccount(String savingsId);
    TotalSavingsResponse getTotalSavings();
    TotalLoansOutstandingResponse getTotalLoansOutstanding();
    TotalLoansOverdueResponse getTotalLoansOverdue();
    TotalInterestCollectedResponse getTotalInterestCollected(Month month, Year year);
}
