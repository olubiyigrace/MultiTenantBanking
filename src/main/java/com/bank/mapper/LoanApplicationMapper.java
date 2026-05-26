package com.bank.mapper;

import com.bank.entities.LoanApplication;
import com.bank.requests.LoanApplicationRequest;
import com.bank.responses.LoanApplicationResponse;
import com.bank.responses.LoanRejectionResponse;
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
                .loanStatus(loanApplication.getLoanStatus())
                .requestedAmount(loanApplication.getRequestedAmount())
                .approvedAmount(loanApplication.getApprovedAmount())
                .tenureMonths(loanApplication.getTenureMonths())
                .purpose(loanApplication.getPurpose())
                .interestRatePercent(loanApplication.getInterestRatePercent())
                .interestType(loanApplication.getInterestType())
                .totalInterest(loanApplication.getTotalInterest())
                .totalRepayable(loanApplication.getTotalRepayable())
                .monthlyInstallment(loanApplication.getMonthlyInstallment())
                .processingFee(loanApplication.getProcessingFee())
                .build();
    }
    public LoanRejectionResponse entityToResponse(LoanApplication loanApplication){
        return LoanRejectionResponse.builder()
                .loanProductId(loanApplication.getLoanProductId())
                .loanStatus(loanApplication.getLoanStatus())
                .rejectionReason(loanApplication.getRejectionReason())
                .build();
    }
}
