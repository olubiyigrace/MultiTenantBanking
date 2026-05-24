package com.bank.services.impl;

import com.bank.config.InstitutionContext;
import com.bank.entities.Institution;
import com.bank.entities.MemberProfile;
import com.bank.entities.SavingsAccount;
import com.bank.enums.SavingsAccountType;
import com.bank.enums.SavingsStatus;
import com.bank.exceptions.InvalidRequestException;
import com.bank.mapper.SavingsMapper;
import com.bank.repositories.InstitutionRepository;
import com.bank.repositories.MemberRepository;
import com.bank.repositories.SavingsRepository;
import com.bank.requests.SavingsAccountRequest;
import com.bank.responses.TotalLoansOutstandingResponse;
import com.bank.responses.TotalSavingsResponse;
import com.bank.services.SavingsService;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;
    private final InstitutionRepository institutionRepository;

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
        newSavingsAccount.setInstitution(Institution.builder().id(institutionId).build());
        savingsRepository.save(newSavingsAccount);

        log.info("Savings account created successfully");
    }

    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            accountNumber.append(random.nextInt(10));
        }
        return accountNumber.toString();
    }

    @Override
    public void activateAccount(String savingsId) {
        log.info("Activating savings account");
        SavingsAccount existingAccount = savingsRepository.findById(savingsId).orElseThrow(() ->
                new InvalidRequestException("Savings account does not exist"));
        if (existingAccount.getSavingsStatus() == SavingsStatus.CLOSED) {
            log.debug("Closed account cannot be reactivated");
            throw new InvalidRequestException("Closed account cannot be reactivated");
        }
        if (existingAccount.getSavingsStatus() == SavingsStatus.ACTIVE) {
            log.debug("Savings account already activated");
            throw new DuplicateRequestException("Savings account already activated");
        }
        existingAccount.setSavingsStatus(SavingsStatus.ACTIVE);
        savingsRepository.save(existingAccount);

        log.info("Savings account activated");
    }

    @Override
    public void freezeAccount(String savingsId) {
        log.info("Freezing savings account");
        SavingsAccount existingAccount = savingsRepository.findById(savingsId).orElseThrow(() ->
                new InvalidRequestException("Savings account does not exist"));
        if (existingAccount.getSavingsStatus() == SavingsStatus.CLOSED) {
            log.debug("Closed account cannot be frozen");
            throw new InvalidRequestException("Closed account cannot be frozen");
        }
        if (existingAccount.getSavingsStatus() == SavingsStatus.FROZEN) {
            log.debug("Savings account already frozen");
            throw new DuplicateRequestException("Savings account already frozen");
        }
        existingAccount.setSavingsStatus(SavingsStatus.FROZEN);
        savingsRepository.save(existingAccount);

        log.info("Savings account frozen");
    }

    @Override
    public void closeAccount(String savingsId) {
        log.info("Closing savings account");
        SavingsAccount existingAccount = savingsRepository.findById(savingsId).orElseThrow(() ->
                new InvalidRequestException("Savings account does not exist"));
        if (existingAccount.getSavingsStatus() == SavingsStatus.CLOSED) {
            log.debug("Savings account already closed");
            throw new DuplicateRequestException("Savings account already closed");
        }
        if (existingAccount.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            log.info("Account with balance cannot be closed");
            throw new InvalidRequestException("Account with balance cannot be closed");
        }
        existingAccount.setSavingsStatus(SavingsStatus.CLOSED);
        savingsRepository.save(existingAccount);

        log.info("Savings account closed");
    }

    @Override
    public TotalSavingsResponse getTotalSavings() {
        log.info("Fetching total savings");
        String institutionId = InstitutionContext.getCurrentInstitution();

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InvalidRequestException("Institution does not exist"));

        BigDecimal totalSavings = calculateTotalSavings();

        return TotalSavingsResponse.builder()
                .institutionId(institution.getId())
                .institutionName(institution.getInstitutionName())
                .totalSavingsBalance(totalSavings)
                .build();
    }

    private BigDecimal calculateTotalSavings() {
        try {
            String sql =
                    """
                            SELECT COALESCE(SUM(balance), 0)
                            FROM savings_accounts
                            WHERE savings_status != 'CLOSED'
                            """;
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating total savings", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public TotalLoansOutstandingResponse getTotalLoansOutstanding() {
        log.info("Fetching total savings");
        String institutionId = InstitutionContext.getCurrentInstitution();
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InvalidRequestException("Institution does not exist"));

            String schema = institution.getInstitutionName().toLowerCase();
            BigDecimal loansOutstanding = getTotalLoansOutstanding(schema);

                    return TotalLoansOutstandingResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(schema)
                            .totalLoansOutstanding(loansOutstanding)
                            .build();

    }

    private BigDecimal getTotalLoansOutstanding(String schema) {
        try {
            String sql = """
            SELECT COALESCE(SUM(balance_remaining), 0)
            FROM %s.loan_repayment_schedule
            """.formatted(schema);
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating total loans outstanding", e);
            return BigDecimal.ZERO;
        }
    }
}





