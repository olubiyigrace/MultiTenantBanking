package com.bank.loanapplications;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class LoanApplicationResponse {
    private String loanProductId;
    private String id;
    private LoanApplicationStatus loanApplicationStatus;
    private BigDecimal requestedAmount;;
    private String purpose;
}
