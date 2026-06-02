package com.bank.transactions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
@AllArgsConstructor
@Builder
public class TotalDepositsStatisticsResponse {
    private BigDecimal totalInstitutionsDeposits;
    private List<TotalDepositsResponse> institutions;
}
