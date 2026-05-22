package com.bank.requests;

import com.bank.enums.InterestType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanProductRequest {
    @NotBlank(message = "Loan product name should not be empty")
    @Column(updatable = false)
    private String name;

    @NotBlank(message = "Product description should not be empty")
    private String description;

    @Positive(message = "Minimum amount must be greater than zero")
    private BigDecimal minAmount;

    @Positive(message = "Maximum amount must be greater than zero")
    private BigDecimal maxAmount;

    @DecimalMin(value = "0.0", inclusive = false, message = "Interest rate must be greater than 0")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100")
    private BigDecimal interestRatePercent;

    @NotNull(message = "Maximum tenure is required")
    @Positive(message = "Maximum tenure must be greater than zero")
    private Integer maxTenureMonths;

    @DecimalMin(value = "0.0",
            message = "Processing fee cannot be negative")
    @DecimalMax(value = "100.0",
            message = "Processing fee cannot exceed 100")
    private BigDecimal processingFeePercent;

    private InterestType interestType;
    private Boolean requiresGuarantor;
    private Boolean requiresCollateral;
    private Boolean isActive;
}
