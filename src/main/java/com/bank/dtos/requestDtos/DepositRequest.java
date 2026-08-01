package com.bank.dtos.requestDtos;

import com.bank.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepositRequest {
    @NotNull
    private BigDecimal amount;

    private String description;

    @NotNull
    private TransactionType transactionType;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Slip number is required")
    private String depositSlipNumber;
}
