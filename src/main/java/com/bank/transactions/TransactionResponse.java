package com.bank.transactions;

import com.bank.institutions.Institution;
import com.bank.users.User;
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
