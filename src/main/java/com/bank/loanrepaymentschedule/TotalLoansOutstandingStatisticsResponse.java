package com.bank.loanrepaymentschedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class TotalLoansOutstandingStatisticsResponse {
    private BigDecimal totalInstitutionsLoansOutstanding;
    private List<TotalLoansOutstandingResponse> institutions;
}

