package com.bank.loanapplications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class TotalInterestCollectedResponse {
    private String institutionId;
    private String institutionName;
    private BigDecimal interestCollected;
}
