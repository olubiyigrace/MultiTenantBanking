package com.bank.services;


import com.bank.dtos.responseDtos.InstitutionResponse;
import com.bank.dtos.responseDtos.TotalLoansDisbursedStatisticsResponse;
import com.bank.dtos.responseDtos.TotalLoansOutstandingStatisticsResponse;
import com.bank.dtos.responseDtos.TotalMembersStatisticsResponse;
import com.bank.dtos.responseDtos.TotalSavingsStatisticsResponse;
import com.bank.dtos.responseDtos.TotalDepositsStatisticsResponse;
import com.bank.utils.PageResponse;
import jakarta.mail.MessagingException;

import java.time.Month;
import java.time.Year;

public interface InstitutionService {
    void approveInstitution(final String institutionId) throws MessagingException;
    void activateInstitution(final String institutionId);
    void suspendInstitution(final String institutionId);
    PageResponse<InstitutionResponse> findAllInstitution(final int page, final int size);
    TotalMembersStatisticsResponse getMembersStatistics();
    TotalSavingsStatisticsResponse getSavingsStatistics();
    TotalLoansOutstandingStatisticsResponse getLoansOutstandingStatistics();
    TotalDepositsStatisticsResponse getDepositsStatistics();
    TotalLoansDisbursedStatisticsResponse getLoansDisbursedStatistics(Month month, Year year);
}
