package com.bank.entities;


import com.bank.common.AbstractEntity;
import com.bank.enums.SavingsAccountType;
import com.bank.enums.SavingsStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "savings_accounts")
public class SavingsAccount extends AbstractEntity {
    private BigDecimal balance;
    private BigDecimal minimumBalance;
    private BigDecimal interestRatePercent;
    private BigDecimal targetAmount;
    private LocalDate maturityDate;

    @Version
    private Integer version;

    @Column(unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private SavingsStatus savingsStatus;

    @Enumerated(EnumType.STRING)
    private SavingsAccountType savingsAccountType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_savings_account_institution_id"))
    private Institution institution;

    @ManyToOne
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_loan_application_member_id"))
    private MemberProfile member;
}
