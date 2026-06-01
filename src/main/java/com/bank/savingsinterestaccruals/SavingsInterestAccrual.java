package com.bank.savingsinterestaccruals;

import com.bank.savingsaccount.SavingsAccount;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "savings_interest_accruals")
public class SavingsInterestAccrual {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "savings_account_id", foreignKey = @ForeignKey(name = "fk_savings_interest_accrual_savings_account_id"))
    private SavingsAccount savingsAccount;

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal openingBalance;
    private BigDecimal interestAmount;
    private LocalDateTime creditedAt;
}
