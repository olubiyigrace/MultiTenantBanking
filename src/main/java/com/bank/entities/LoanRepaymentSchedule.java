package com.bank.entities;

import com.bank.enums.LoanRepaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanRepaymentSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String installmentNumber;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private BigDecimal totalDue;
    private BigDecimal amountPaid;
    private BigDecimal balanceRemaining;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    private LoanRepaymentStatus loanRepaymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", foreignKey = @ForeignKey(name = "fk_loan_repayment_schedule_loan_application_id"))
    private LoanApplication loanApplication;
}
