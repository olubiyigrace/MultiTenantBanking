package com.bank.loanproducts;

import org.springframework.stereotype.Component;

@Component
public class LoanProductMapper {
    public LoanProduct toEntity(LoanProductRequest loanProductRequest){
        return LoanProduct.builder()
                .name(loanProductRequest.getName())
                .description(loanProductRequest.getDescription())
                .minAmount(loanProductRequest.getMinAmount())
                .maxAmount(loanProductRequest.getMaxAmount())
                .interestRatePercent(loanProductRequest.getInterestRatePercent())
                .maxTenureMonths(loanProductRequest.getMaxTenureMonths())
                .requiresGuarantor(loanProductRequest.getRequiresGuarantor())
                .requiresCollateral(loanProductRequest.getRequiresCollateral())
                .processingFeePercent(loanProductRequest.getProcessingFeePercent())
                .interestType(loanProductRequest.getInterestType())
                .build();
    }

    public LoanProductResponse toResponse(LoanProduct loanProduct){
        return LoanProductResponse.builder()
                .id(loanProduct.getId())
                .name(loanProduct.getName())
                .description(loanProduct.getDescription())
                .minAmount(loanProduct.getMinAmount())
                .maxAmount(loanProduct.getMaxAmount())
                .interestRatePercent(loanProduct.getInterestRatePercent())
                .maxTenureMonths(loanProduct.getMaxTenureMonths())
                .requiresGuarantor(loanProduct.getRequiresGuarantor())
                .requiresCollateral(loanProduct.getRequiresCollateral())
                .processingFeePercent(loanProduct.getProcessingFeePercent())
                .interestType(loanProduct.getInterestType())
                .build();
    }
}
