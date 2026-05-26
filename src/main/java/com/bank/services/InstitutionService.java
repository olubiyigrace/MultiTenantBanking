package com.bank.services;


import com.bank.auth.response.UserResponse;
import com.bank.responses.PageResponse;
import com.bank.responses.*;
import jakarta.mail.MessagingException;

import java.time.Month;
import java.time.Year;
import java.util.List;

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
