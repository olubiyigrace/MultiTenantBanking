package com.bank.others.utils.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class TotalDepositsResponse {
    private String institutionId;
    private String institutionName;
    private BigDecimal totalDeposits;
}
