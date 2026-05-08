package com.bank.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    private SavingsAccount savingsAccount;

    private String periodStart;
    private String periodEnd;
    private String openingBalance;
    private String interestAmount;
    private String creditedAt;
}
