package com.bank.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class TotalLoansOutstandingResponse {
    private String institutionId;
    private String institutionName;
    private BigDecimal totalLoansOutstanding;
}
