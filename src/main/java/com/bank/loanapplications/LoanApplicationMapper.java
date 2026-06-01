package com.bank.loanapplications;

import org.springframework.stereotype.Component;

@Component
public class LoanApplicationMapper {
    public LoanApplication toEntity(LoanApplicationRequest loanApplicationRequest){
        return LoanApplication.builder()
                .loanProductId(loanApplicationRequest.getLoanProductId())
                .requestedAmount(loanApplicationRequest.getRequestedAmount())
                .purpose(loanApplicationRequest.getPurpose())
                .build();
    }

    public LoanApplicationResponse toResponse(LoanApplication loanApplication){
        return LoanApplicationResponse.builder()
                .loanProductId(loanApplication.getLoanProductId())
                .id(loanApplication.getId())
                .loanApplicationStatus(LoanApplicationStatus.PENDING)
                .purpose(loanApplication.getPurpose())
                .requestedAmount(loanApplication.getRequestedAmount())
                .build();
    }
}
