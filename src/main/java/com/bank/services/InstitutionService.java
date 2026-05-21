package com.bank.services;


import com.bank.common.PageResponse;
import com.bank.responses.*;

public interface InstitutionService {
    void approveInstitution(final String institutionId);
    void activateInstitution(final String institutionId);
    void deactivateInstitution(final String institutionId);
    void suspendInstitution(final String institutionId);
    PageResponse<InstitutionResponse> findAllInstitution(final int page, final int size);
    TotalMembersStatisticsResponse getMembersStatistics();
    TotalSavingsStatisticsResponse getSavingsStatistics();
    TotalLoansOutstandingStatisticsResponse getLoansOutstandingStatistics();
    TotalDepositsStatisticsResponse getDepositsStatistics();
    TotalLoansDisbursedStatisticsResponse getLoansDisbursedStatistics(java.time.Month month, java.time.Year year);
}
