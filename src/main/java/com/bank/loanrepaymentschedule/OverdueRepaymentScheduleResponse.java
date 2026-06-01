package com.bank.loanrepaymentschedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OverdueRepaymentScheduleResponse {
    private String repaymentScheduleId;
    private String loanApplicationId;
    private String memberName;

    private BigDecimal installmentAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceRemaining;

    private LocalDate dueDate;
    private LoanRepaymentStatus repaymentStatus;
}
