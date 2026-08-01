package com.bank.dtos.responseDtos;

import com.bank.entities.Institution;
import com.bank.entities.User;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private String reference;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private User user;
    private Institution institution;
}
