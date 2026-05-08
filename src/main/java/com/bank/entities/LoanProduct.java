package com.bank.entities;

import com.bank.common.AbstractEntity;
import com.bank.enums.InterestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "loan_products")
public class LoanProduct extends AbstractEntity {
    private String name;
    private String description;
    private String minAmount;
    private String maxAmount;
    private String interestRatePercent;
    private InterestType interestType;
    private String maxTenureMonths;
    private Boolean requiresGuarantor;
    private Boolean requiresCollateral;
    private String processingFeePercent;
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;

    @OneToMany(mappedBy = "loanProduct")
    private List<LoanApplication> loanApplication;
}
