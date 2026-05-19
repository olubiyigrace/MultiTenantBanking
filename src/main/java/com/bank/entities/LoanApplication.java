package com.bank.entities;

import com.bank.common.AbstractEntity;
import com.bank.enums.LoanApplicationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "loan_applications")
public class LoanApplication extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;

    @OneToOne(fetch = FetchType.LAZY)
    private MemberProfile memberProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    private LoanProduct loanProduct;

    @Enumerated(EnumType.STRING)
    private LoanApplicationStatus loanStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_officer")
    private User user;

    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private Integer tenureMonths;
    private String purpose;
    private BigDecimal interestRatePercent;
    private String interestType;
    private BigDecimal totalInterest;
    private BigDecimal totalRepayable;
    private BigDecimal monthlyInstallment;
    private BigDecimal processingFee;
    private BigDecimal netDisbursement;
    private String rejectionReason;
    private UUID reviewedBy;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime disbursedAt;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime fullyRepaidAt;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime reviewedAt;
}
