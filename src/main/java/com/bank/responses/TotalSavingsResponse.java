package com.bank.responses;

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
    private BigDecimal totalSavings;
}
