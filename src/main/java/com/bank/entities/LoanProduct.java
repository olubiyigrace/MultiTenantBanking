package com.bank.entities;

import com.bank.common.AbstractEntity;
import com.bank.enums.InterestType;
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
    private Integer maxTenureMonths;
    private Boolean requiresGuarantor;
    private Boolean requiresCollateral;
    private BigDecimal processingFeePercent;
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private InterestType interestType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;
}
