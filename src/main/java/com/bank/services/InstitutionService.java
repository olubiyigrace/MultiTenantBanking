package com.bank.services;


import com.bank.common.PageResponse;
import com.bank.entities.Institution;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.responses.InstitutionResponse;
import com.bank.responses.TotalDepositsStatisticsResponse;
import com.bank.responses.TotalMemberResponse;
import com.bank.responses.TotalMembersStatisticsResponse;
import jakarta.mail.MessagingException;

import java.math.BigDecimal;
import java.util.List;

public interface InstitutionService {
    void approveInstitution(final String institutionId);
    void activateInstitution(final String institutionId);
    void deactivateInstitution(final String institutionId);
    void suspendInstitution(final String institutionId);
    PageResponse<InstitutionResponse> findAllInstitution(final int page, final int size);
    TotalMembersStatisticsResponse getMembersStatistics();
    TotalDepositsStatisticsResponse getDepositsStatistics();
}
