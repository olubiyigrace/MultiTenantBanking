package com.bank.responses;

import com.bank.enums.InterestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoanProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRatePercent;
    private Integer maxTenureMonths;
    private Boolean requiresGuarantor;
    private Boolean requiresCollateral;
    private BigDecimal processingFeePercent;
    private Boolean isActive;
    private InterestType interestType;
}
