package com.bank.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class TotalDepositResponse {
    private String institutionId;
    private String institutionName;
    private BigDecimal totalDeposits;
}
