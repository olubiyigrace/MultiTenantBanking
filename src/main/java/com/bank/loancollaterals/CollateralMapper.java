package com.bank.loancollaterals;

import org.springframework.stereotype.Component;

@Component
public class CollateralMapper {
    public LoanCollateral toEntity(LoanCollateralRequest loanCollateralRequest){
        return LoanCollateral.builder()
                .description(loanCollateralRequest.getDescription())
                .estimatedValue(loanCollateralRequest.getEstimatedValue())
                .documentUrl(loanCollateralRequest.getDocumentUrl())
                .build();
    }

    public LoanCollateralResponse toResponse(LoanCollateral loanCollateral){
        return LoanCollateralResponse.builder()
                .id(loanCollateral.getId())
                .description(loanCollateral.getDescription())
                .estimatedValue(loanCollateral.getEstimatedValue())
                .documentUrl(loanCollateral.getDocumentUrl())
                .build();
    }
}
