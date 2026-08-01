package com.bank.dtos.responseDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class TotalSavingsResponse {
    private String institutionId;
    private String institutionName;
    private BigDecimal totalSavingsBalance;
}
