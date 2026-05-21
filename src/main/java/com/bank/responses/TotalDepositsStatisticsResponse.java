package com.bank.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class TotalDepositsStatisticsResponse {

    private BigDecimal totalDepositsAcrossAllInstitutions;

    private List<TotalDepositResponse> institutions;
}
