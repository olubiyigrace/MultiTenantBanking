package com.bank.services.impl;

import com.bank.config.InstitutionContext;
import com.bank.entities.Institution;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsServiceImpl implements SavingsService {
    private final SavingsRepository savingsRepository;
    private final SavingsMapper savingsMapper;
    private final MemberRepository memberRepository;


    @Override
    public void create(SavingsAccountRequest savingsAccountRequest) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Creating savings account");
        MemberProfile member = memberRepository.findById(savingsAccountRequest.getMember_id())
                .orElseThrow(() -> {
                    log.debug("Member does not exist");
                    return new InvalidRequestException("Member does not exist");
                });

        boolean exists = savingsRepository.existsByMemberIdAndSavingsAccountType(
                        member.getId(), savingsAccountRequest.getSavingsAccountType());
        if (exists) {
            log.debug("Savings account type already exists for member");
            throw new DuplicateRequestException("Savings account type already exists for member");
        }

        if (savingsAccountRequest.getSavingsAccountType() == SavingsAccountType.TARGET
                && savingsAccountRequest.getTargetAmount() == null) {
            log.debug("Target amount is required for TARGET savings account");
            throw new InvalidRequestException("Target amount is required for TARGET savings account");
        }
        if (savingsAccountRequest.getSavingsAccountType() == SavingsAccountType.FIXED
                && savingsAccountRequest.getMaturityDate() == null) {
            log.debug("Maturity date is required for FIXED savings account");
            throw new InvalidRequestException("Maturity date is required for FIXED savings account");
        }

        SavingsAccount newSavingsAccount = savingsMapper.toEntity(savingsAccountRequest);
        switch (newSavingsAccount.getSavingsAccountType()) {
            case TARGET -> newSavingsAccount.setMinimumBalance(BigDecimal.valueOf(5_000));
            case FIXED -> newSavingsAccount.setMinimumBalance(BigDecimal.valueOf(50_000));
        }
        if (newSavingsAccount.getBalance().compareTo(newSavingsAccount.getMinimumBalance()) < 0) {
            log.debug("Balance cannot be less than minimum balance");
            throw new InvalidRequestException("Balance cannot be less than minimum balance");
        }
        newSavingsAccount.setAccountNumber(generateAccountNumber());
        newSavingsAccount.setInterestRatePercent(BigDecimal.valueOf(4.50));
        newSavingsAccount.setSavingsStatus(SavingsStatus.ACTIVE);
        newSavingsAccount.setInstitution(Institution.builder().id(institutionId).build());
        savingsRepository.save(newSavingsAccount);

        log.info("Savings account created successfully");
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

