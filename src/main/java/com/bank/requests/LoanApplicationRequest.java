package com.bank.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LoanApplicationRequest {

    @NotBlank(message = "Loan application purpose should not be empty")
    private String loanProductId;

    @NotNull(message = "Requested amount cannot be null")
    private BigDecimal requestedAmount;

    @NotBlank(message = "Loan application purpose should not be empty")
    private String purpose;
}
