package com.bank.mapper;

import com.bank.entities.SavingsAccount;
import com.bank.requests.SavingsAccountRequest;
import org.springframework.stereotype.Component;

@Component
public class SavingsMapper {
    public SavingsAccount toEntity(SavingsAccountRequest savingsAccountRequest){
        return SavingsAccount.builder()
                .balance(savingsAccountRequest.getBalance())
                .targetAmount(savingsAccountRequest.getTargetAmount())
                .maturityDate(savingsAccountRequest.getMaturityDate())
                .savingsAccountType(savingsAccountRequest.getSavingsAccountType())
                .memberId(savingsAccountRequest.getMember_id())
                .build();
    }
}
