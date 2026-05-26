package com.bank.requests;

import com.bank.enums.SavingsAccountType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class SavingsAccountRequest {
    @NotNull(message = "Initial balance is required")
    @PositiveOrZero(message = "Minimum balance cannot be negative")
    @DecimalMin(value = "0.0")
    private BigDecimal balance;

    @PositiveOrZero(message = "Target amount cannot be negative")
    @Digits(integer = 15, fraction = 2, message = "Target amount must be a valid monetary amount")
    private final BigDecimal targetAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Future(message = "Maturity date must be a future date")
    private LocalDate maturityDate;

    @NotNull(message = "Savings account type is required")
    private SavingsAccountType savingsAccountType;

    @NotNull(message = "Member profile ID is required")
    private String memberId;

}


