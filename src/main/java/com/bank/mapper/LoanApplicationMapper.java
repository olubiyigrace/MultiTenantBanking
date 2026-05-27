package com.bank.mapper;

import com.bank.entities.LoanApplication;
import com.bank.enums.LoanApplicationStatus;
import com.bank.requests.LoanApplicationRequest;
import com.bank.responses.LoanApplicationResponse;
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
