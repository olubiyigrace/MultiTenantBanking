package com.bank.savingsaccount;

import org.springframework.stereotype.Component;

@Component
public class SavingsMapper {
    public SavingsAccount toEntity(SavingsAccountRequest savingsAccountRequest){
        return SavingsAccount.builder()
                .balance(savingsAccountRequest.getBalance())
                .targetAmount(savingsAccountRequest.getTargetAmount())
                .maturityDate(savingsAccountRequest.getMaturityDate())
                .savingsAccountType(savingsAccountRequest.getSavingsAccountType())
                .build();
    }
}
