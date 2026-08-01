package com.bank.mappers;

import com.bank.entities.SavingsAccount;
import com.bank.dtos.requestDtos.SavingsAccountRequest;
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
