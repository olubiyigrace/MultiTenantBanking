package com.bank.services.impl;

import com.bank.config.InstitutionContext;
import com.bank.entities.Institution;
import com.bank.entities.SavingsAccount;
import com.bank.enums.SavingsStatus;
import com.bank.exceptions.InvalidRequestException;
import com.bank.repositories.InstitutionRepository;
import com.bank.repositories.SavingsRepository;
import com.bank.responses.TotalInterestCollectedResponse;
import com.bank.responses.TotalLoansOutstandingResponse;
import com.bank.responses.TotalLoansOverdueResponse;
import com.bank.responses.TotalSavingsResponse;
import com.bank.services.SavingsService;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsServiceImpl implements SavingsService {
    private final SavingsRepository savingsRepository;
    private final JdbcTemplate jdbcTemplate;
    private final InstitutionRepository institutionRepository;


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
        log.info("Getting total savings");
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
        log.info("Getting total loans outstanding");
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

    @Override
    public TotalLoansOverdueResponse getTotalLoansOverdue() {
        log.info("Getting total loans overdue");
        String institutionId = InstitutionContext.getCurrentInstitution();
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InvalidRequestException("Institution does not exist"));

        String schema = institution.getInstitutionName().toLowerCase();
        BigDecimal loansOverdue = getTotalLoansOverdue(schema);

        return TotalLoansOverdueResponse.builder()
                .institutionId(institution.getId())
                .institutionName(schema)
                .totalLoansOverdue(loansOverdue)
                .build();

    }

    private BigDecimal getTotalLoansOverdue(String schema) {
        try {
            String sql = """
                    SELECT COALESCE(SUM(loan_repayment_status.OVERDUE), 0)
                    FROM %s.loan_repayment_schedule
                    """.formatted(schema);
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating total loans outstanding", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public TotalInterestCollectedResponse getTotalInterestCollected(Month month, Year year) {
        log.info("Getting total interest for the month");
        String institutionId = InstitutionContext.getCurrentInstitution();
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InvalidRequestException("Institution does not exist"));

        String schema = institution.getInstitutionName().toLowerCase();
        BigDecimal interestCollected = getTotalInterest(schema, month, year);

        return TotalInterestCollectedResponse.builder()
                .institutionId(institution.getId())
                .institutionName(schema)
                .interestCollected(interestCollected)
                .build();

    }

    private BigDecimal getTotalInterest(String schema, Month month, Year year) {
        try {
            StringBuilder sqlBuilder = new StringBuilder(
                    "SELECT COALESCE(SUM(total_interest), 0) FROM %s.loan_applications WHERE 1=1 ".formatted(schema)
            );
            List<Object> params = new ArrayList<>();
            if (year != null) {
                sqlBuilder.append("AND EXTRACT(YEAR FROM created_at) = ? ");
                params.add(year);
            }
            if (month != null) {
                sqlBuilder.append("AND EXTRACT(MONTH FROM created_at) = ? ");
                params.add(month);
            }
            BigDecimal total = jdbcTemplate.queryForObject(sqlBuilder.toString(), BigDecimal.class, params.toArray());
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}