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
    @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_loan_application_institution_id"))
    private Institution institution;

    @ManyToOne
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_loan_application_member_id"))
    private MemberProfile member;

    @ManyToOne
    @JoinColumn(name = "loan_officer_id", foreignKey = @ForeignKey(name = "fk_loan_application_loan_officer_id"))
    private User loanOfficer;

    @Enumerated(EnumType.STRING)
    private LoanApplicationStatus loanStatus;

    private String loanProductId;
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
