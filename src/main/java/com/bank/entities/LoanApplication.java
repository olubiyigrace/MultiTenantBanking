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
import org.hibernate.sql.results.graph.Fetch;

import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(mappedBy = "loanApplication")
    private List<LoanGuarantors> loanGuarantors;

    @OneToMany(mappedBy = "loanApplication")
    private List<LoanCollateral> loanCollateral;

    @OneToMany(mappedBy = "loanApplication")
    private List<LoanRepaymentSchedule> loanRepaymentSchedule;

    @Enumerated(EnumType.STRING)
    private LoanApplicationStatus loanStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_officer")
    private User user;

    private String requestedAmount;
    private String approvedAmount;
    private String tenureMonths;
    private String purpose;
    private String interestRatePercent;
    private String interestType;
    private String totalInterest;
    private String totalRepayable;
    private String monthlyInstallment;
    private String processingFee;
    private String netDisbursement;
    private String rejectionReason;
    private String reviewedBy;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime disbursedAt;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime fullyRepaidAt;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime reviewedAt;
}
