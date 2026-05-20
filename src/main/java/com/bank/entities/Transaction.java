package com.bank.entities;

import com.bank.common.AbstractEntity;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "transactions")
public class Transaction extends AbstractEntity {
    private UUID reversedByTransactionId;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;

    @Column(unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_user_id", foreignKey = @ForeignKey(name = "fk_transaction_performed_by_user_id"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savings_account_id", foreignKey = @ForeignKey(name = "fk_transaction_savings_account_id"))
    private SavingsAccount savingsAccount;
}