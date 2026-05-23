package com.bank.requests;

import com.bank.enums.InterestType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class LoanProductRequest {
    @NotBlank(message = "Loan product name should not be empty")
    @Column(updatable = false)
    private String name;

    @NotBlank(message = "Product description should not be empty")
    private String description;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRatePercent;
    private Integer maxTenureMonths;
    private BigDecimal processingFeePercent;
    private InterestType interestType;
    private Boolean requiresGuarantor;
    private Boolean requiresCollateral;
    private Boolean isActive;
}
