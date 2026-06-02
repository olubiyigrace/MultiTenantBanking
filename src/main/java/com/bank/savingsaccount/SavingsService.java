package com.bank.savingsaccount;

import com.bank.loanapplications.TotalInterestCollectedResponse;
import com.bank.loanrepaymentschedule.TotalLoansOutstandingResponse;
import com.bank.loanrepaymentschedule.TotalLoansOverdueResponse;

import java.time.Month;
import java.time.Year;

public interface SavingsService {
    void activateAccount(String savingsId);
    void freezeAccount(String savingsId);
    void closeAccount(String savingsId);
    TotalSavingsResponse getTotalSavings();
    TotalLoansOutstandingResponse getTotalLoansOutstanding();
    TotalLoansOverdueResponse getTotalLoansOverdue();
    TotalInterestCollectedResponse getTotalInterestCollected(Month month, Year year);
}
