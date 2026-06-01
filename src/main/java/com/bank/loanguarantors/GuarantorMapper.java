package com.bank.loanguarantors;

import org.springframework.stereotype.Component;

@Component
public class GuarantorMapper {
    public LoanGuarantor toEntity(GuarantorRequest guarantorRequest){
        return LoanGuarantor.builder()
                .guarantorMemberId(guarantorRequest.getGuarantorMemberId())
                .guarantorStatus(GuarantorStatus.PENDING)
                .build();
    }

    public GuarantorResponse toResponse(LoanGuarantor loanGuarantor){
        return GuarantorResponse.builder()
                .id(loanGuarantor.getId())
                .guarantorMemberId(loanGuarantor.getGuarantorMemberId())
                .guarantorStatus(loanGuarantor.getGuarantorStatus())
                .respondedAt(loanGuarantor.getRespondedAt())
                .build();
    }
}
