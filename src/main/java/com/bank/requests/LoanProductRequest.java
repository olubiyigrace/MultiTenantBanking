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

    @NotBlank(message = "Minimum amount should not be empty")
    private BigDecimal minAmount;

    @NotBlank(message = "Maximum amount should not be empty")
    private BigDecimal maxAmount;

    @NotBlank(message = "Interest rate percentage should not be empty")
    private BigDecimal interestRatePercent;

    @NotBlank(message = "Maximum tenure months should not be empty")
    private Integer maxTenureMonths;

    @NotBlank(message = "State whether it requires a guarantor or not")
    private Boolean requiresGuarantor;

    @NotBlank(message = "State whether it requires collateral or not")
    private Boolean requiresCollateral;

    @NotBlank(message = "Processing fee percentage should not be empty")
    private BigDecimal processingFeePercent;

    @NotBlank(message = "State whether loan product is active")
    private Boolean isActive;

    @NotBlank(message = "Interest type is required")
    private InterestType interestType;
}
