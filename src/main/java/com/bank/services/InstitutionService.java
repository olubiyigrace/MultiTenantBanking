package com.bank.services;


import com.bank.common.PageResponse;
import com.bank.responses.InstitutionResponse;
import com.bank.responses.TotalSavingsStatisticsResponse;
import com.bank.responses.TotalMembersStatisticsResponse;

public interface InstitutionService {
    void approveInstitution(final String institutionId);
    void activateInstitution(final String institutionId);
    void deactivateInstitution(final String institutionId);
    void suspendInstitution(final String institutionId);
    PageResponse<InstitutionResponse> findAllInstitution(final int page, final int size);
    TotalMembersStatisticsResponse getMembersStatistics();
    TotalSavingsStatisticsResponse getSavingsStatistics() ;
}
