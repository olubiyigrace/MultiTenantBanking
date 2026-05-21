package com.bank.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class TotalLoansDisbursedStatisticsResponse {
    private BigDecimal totalInstitutionsLoansDisbursed;
    private List<TotalLoansDisbursedResponse> institutions;
}
