package com.bank.loanapplications;

import com.bank.others.entity.AbstractEntity;
import com.bank.memberprofiles.MemberProfile;
import com.bank.users.User;
import com.bank.loanproducts.InterestType;
import com.bank.institutions.Institution;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private LoanApplicationStatus loanApplicationStatus;

    @Enumerated(EnumType.STRING)
    private InterestType interestType;

    private String loanProductId;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal tenureMonths;
    private String purpose;
    private BigDecimal interestRatePercent;
    private BigDecimal totalInterest;
    private BigDecimal totalRepayable;
    private BigDecimal monthlyInstallment;
    private BigDecimal processingFee;
    private BigDecimal netDisbursement;
    private String rejectionReason;
    private String reviewedBy;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime disbursedAt;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime fullyRepaidAt;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime reviewedAt;
}
