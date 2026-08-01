package com.bank.mappers;

import com.bank.entities.LoanGuarantor;
import com.bank.dtos.requestDtos.GuarantorRequest;
import com.bank.dtos.responseDtos.GuarantorResponse;
import com.bank.enums.GuarantorStatus;
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
