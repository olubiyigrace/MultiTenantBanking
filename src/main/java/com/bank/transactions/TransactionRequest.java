package com.bank.transactions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @NotNull
    private BigDecimal amount;

    private String description;

    @NotNull
    private TransactionType transactionType;

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String name;
}
