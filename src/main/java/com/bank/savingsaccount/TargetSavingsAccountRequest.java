package com.bank.savingsaccount;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetSavingsAccountRequest {

    @NotNull(message = "Savings account type is required")
    private SavingsAccountType savingsAccountType;

    private BigDecimal targetAmount;
    private LocalDate maturityDate;
}
