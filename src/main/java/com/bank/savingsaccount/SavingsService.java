package com.bank.savingsaccount;

import com.bank.others.utils.responses.TotalInterestCollectedResponse;
import com.bank.others.utils.responses.TotalLoansOutstandingResponse;
import com.bank.others.utils.responses.TotalLoansOverdueResponse;
import com.bank.others.utils.responses.TotalSavingsResponse;

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
