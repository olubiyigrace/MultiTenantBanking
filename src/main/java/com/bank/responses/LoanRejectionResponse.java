package com.bank.responses;

import com.bank.enums.LoanApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LoanRejectionResponse {
    private String memberId;
    private String loanProductId;
    private LoanApplicationStatus loanApplicationStatus;
    private String rejectionReason;
}
