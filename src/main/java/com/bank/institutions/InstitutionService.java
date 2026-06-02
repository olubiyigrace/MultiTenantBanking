package com.bank.institutions;


import com.bank.loanapplications.TotalLoansDisbursedStatisticsResponse;
import com.bank.loanrepaymentschedule.TotalLoansOutstandingStatisticsResponse;
import com.bank.memberprofiles.TotalMembersStatisticsResponse;
import com.bank.savingsaccount.TotalSavingsStatisticsResponse;
import com.bank.transactions.TotalDepositsStatisticsResponse;
import com.bank.users.UserResponse;
import com.bank.others.utils.PageResponse;
import jakarta.mail.MessagingException;

import java.time.Month;
import java.time.Year;

public interface InstitutionService {
    void approveInstitution(final String institutionId) throws MessagingException;
    void activateInstitution(final String institutionId);
    void deactivateInstitution(final String institutionId);
    void suspendInstitution(final String institutionId);
    PageResponse<InstitutionResponse> findAllInstitution(final int page, final int size);
    TotalMembersStatisticsResponse getMembersStatistics();
    TotalSavingsStatisticsResponse getSavingsStatistics();
    TotalLoansOutstandingStatisticsResponse getLoansOutstandingStatistics();
    TotalDepositsStatisticsResponse getDepositsStatistics();
    TotalLoansDisbursedStatisticsResponse getLoansDisbursedStatistics(Month month, Year year);
    PageResponse<UserResponse> getAllUsers(final int page, final int size);
}
