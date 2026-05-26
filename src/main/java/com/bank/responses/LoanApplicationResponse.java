package com.bank.responses;

import com.bank.entities.Institution;
import com.bank.entities.User;
import com.bank.enums.LoanApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class LoanApplicationResponse {
    private String loanProductId;
    private LoanApplicationStatus loanStatus;
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
}
