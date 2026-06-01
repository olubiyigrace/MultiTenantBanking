package com.bank.loanproducts;

import com.bank.others.AbstractEntity;
import com.bank.institutions.Institution;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

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
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRatePercent;
    private BigDecimal maxTenureMonths;
    private Boolean requiresGuarantor;
    private Boolean requiresCollateral;
    private BigDecimal processingFeePercent;
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private InterestType interestType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_loan_product_institution_id"))
    private Institution institution;
}
