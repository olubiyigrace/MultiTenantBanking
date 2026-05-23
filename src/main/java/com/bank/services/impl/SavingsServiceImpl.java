package com.bank.services.impl;

import com.bank.entities.MemberProfile;
import com.bank.entities.SavingsAccount;
import com.bank.enums.SavingsAccountType;
import com.bank.enums.SavingsStatus;
import com.bank.exceptions.InvalidRequestException;
import com.bank.mapper.SavingsMapper;
import com.bank.repositories.MemberRepository;
import com.bank.repositories.SavingsRepository;
import com.bank.requests.SavingsAccountRequest;
import com.bank.services.SavingsService;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsServiceImpl implements SavingsService {
    private final SavingsRepository savingsRepository;
    private final SavingsMapper savingsMapper;
    private final MemberRepository memberRepository;


    @Override
    public void create(SavingsAccountRequest savingsAccountRequest) {
        log.info("Creating savings account");
        Optional<MemberProfile> confirmMember = memberRepository.findById(savingsAccountRequest.getMember_id());
        if (confirmMember.isEmpty()) {
            log.debug("Member does not exist");
            throw new InvalidRequestException("Member does not exist");
        }

        MemberProfile confirmAccountType = memberRepository.findBySavingsAccountType(savingsAccountRequest.getSavingsAccountType());
        if (confirmAccountType.getSavingsAccountType() == SavingsAccountType.REGULAR) {
            log.debug("Savings account type already exists for member");
            throw new DuplicateRequestException("Savings account type already exists for member");
        }

        Optional<SavingsAccount> confirmSavingsAccount = savingsRepository.findById(savingsAccountRequest.getMember_id());
        if (confirmSavingsAccount.equals(SavingsAccountType.FIXED)) {
            log.debug("Savings account type already exists for member");
            throw new DuplicateRequestException("Savings account type already exists for member");
        }
        if (confirmSavingsAccount.equals(SavingsAccountType.TARGET)) {
            log.debug("Savings account type already exists for member");
            throw new DuplicateRequestException("Savings account type already exists for member");
        }

        if (savingsAccountRequest.getMaturityDate() == null &&
                savingsAccountRequest.getSavingsAccountType() == SavingsAccountType.TARGET) {
            log.debug("Maturity date is required for TARGET savings account");
            throw new InvalidRequestException("Maturity date is required for TARGET savings account");
        }
        if (savingsAccountRequest.getTargetAmount() == null &&
                savingsAccountRequest.getSavingsAccountType() == SavingsAccountType.FIXED) {
            log.debug("Target amount is required for FIXED savings account");
            throw new InvalidRequestException("Target amount is required for FIXED savings account");
        }
        SavingsAccount newSavingsAccount = savingsMapper.toEntity(savingsAccountRequest);
        if(newSavingsAccount.getSavingsAccountType() == SavingsAccountType.FIXED){
            newSavingsAccount.setMinimumBalance(BigDecimal.valueOf(50_000));
        }
        System.out.println("okay");
        if(newSavingsAccount.getSavingsAccountType() == SavingsAccountType.TARGET){
            newSavingsAccount.setMinimumBalance(BigDecimal.valueOf(5000));
        }
        newSavingsAccount.setAccountNumber(generateAccountNumber());
        newSavingsAccount.setSavingsStatus(SavingsStatus.ACTIVE);
        savingsRepository.save(newSavingsAccount);

        log.info("Savings account created");
    }
    private String generateAccountNumber () {
        SecureRandom random = new SecureRandom();
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            accountNumber.append(random.nextInt(10));
        }
        return accountNumber.toString();
    }
}

