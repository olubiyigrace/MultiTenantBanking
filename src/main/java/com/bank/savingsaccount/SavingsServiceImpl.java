package com.bank.savingsaccount;

import com.bank.others.auditlogs.AuditLog;
import com.bank.others.auditlogs.AuditLogRepository;
import com.bank.others.config.InstitutionContext;
import com.bank.institutions.Institution;
import com.bank.others.exceptions.InvalidRequestException;
import com.bank.institutions.InstitutionRepository;
import com.bank.others.utils.CurrentUserUtil;
import com.bank.loanapplications.TotalInterestCollectedResponse;
import com.bank.loanrepaymentschedule.TotalLoansOutstandingResponse;
import com.bank.loanrepaymentschedule.TotalLoansOverdueResponse;
import com.bank.users.User;
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
    private final AuditLogRepository auditLogRepository;
    private final CurrentUserUtil currentUserUtil;


    @Override
    public void activateAccount(String savingsId) {
        User currentUser = currentUserUtil.getLoggedInUser();

        log.info("Activating savings account");
        SavingsAccount existingAccount = savingsRepository.findById(savingsId).orElseThrow(() ->
                new InvalidRequestException("Savings account does not exist"));

        SavingsStatus oldStatus = existingAccount.getSavingsStatus();

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

        AuditLog auditLog = AuditLog.builder()
                .institution(existingAccount.getInstitution())
                .user(currentUser)
                .entityType("SAVINGS_ACCOUNT")
                .action("UNFREEZE")
                .entityId(existingAccount.getId())
                .oldValue(oldStatus.name())
                .ipAddress("none")
                .newValue(SavingsStatus.ACTIVE.name())
                .build();
        auditLogRepository.save(auditLog);

        log.info("Savings account activated");
    }

    @Override
    public void freezeAccount(String savingsId) {
        User currentUser = currentUserUtil.getLoggedInUser();
        log.info("Freezing savings account");

        SavingsAccount existingAccount = savingsRepository.findById(savingsId).orElseThrow(() ->
                new InvalidRequestException("Savings account does not exist"));

        SavingsStatus oldStatus = existingAccount.getSavingsStatus();

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

        AuditLog auditLog = AuditLog.builder()
                .institution(existingAccount.getInstitution())
                .user(currentUser)
                .entityType("SAVINGS_ACCOUNT")
                .action("FREEZE")
                .entityId(existingAccount.getId())
                .oldValue(oldStatus.name())
                .ipAddress("none")
                .newValue(SavingsStatus.FROZEN.name())
                .build();
        auditLogRepository.save(auditLog);
        log.info("Savings account frozen");
    }

    @Override
    public void closeAccount(String savingsId) {
        log.info("Closing savings account");
        SavingsAccount existingAccount = savingsRepository.findById(savingsId).orElseThrow(() ->
                new InvalidRequestException("Savings account does not exist"));
        if (existingAccount.getSavingsStatus() == SavingsStatus.CLOSED) {
            log.debug("Savings account has already been closed");
            throw new DuplicateRequestException("Savings account has already been closed");
        }
        if (existingAccount.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            log.info("Account cannot be closed");
            throw new InvalidRequestException("Account cannot be closed");
        }
        existingAccount.setSavingsStatus(SavingsStatus.CLOSED);
        savingsRepository.save(existingAccount);

        log.info("Savings account closed");
    }

    @Override
    public TotalSavingsResponse getTotalSavings() {
        log.info("Getting total savings for current institution");
        String institutionId = InstitutionContext.getCurrentInstitution();

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new InvalidRequestException("Institution does not exist"));

        BigDecimal totalSavings = calculateTotalSavings(institutionId);
        return TotalSavingsResponse.builder()
                .institutionId(institution.getId())
                .institutionName(institution.getInstitutionName())
                .totalSavingsBalance(totalSavings)
                .build();
    }

    private BigDecimal calculateTotalSavings(String institutionId) {
        try {
            String sql = "SELECT COALESCE(SUM(balance), 0) FROM savings_accounts WHERE institution_id = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{institutionId}, BigDecimal.class);
        } catch (Exception e) {
            log.error("Error calculating total savings for institution {}", institutionId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public TotalLoansOutstandingResponse getTotalLoansOutstanding() {
        log.info("Getting total loans outstanding");
        String institutionId = InstitutionContext.getCurrentInstitution();

        Institution institution = institutionRepository.findById(institutionId).orElseThrow(() -> new InvalidRequestException("Institution does not exist"));
        BigDecimal loansOutstanding = getLoansOutstanding();
        return TotalLoansOutstandingResponse.builder()
                .institutionId(institution.getId())
                .institutionName(institution.getInstitutionName())
                .totalLoansOutstanding(loansOutstanding)
                .build();
    }

    private BigDecimal getLoansOutstanding() {
        try {
            String sql = """
                SELECT COALESCE(SUM(balance_remaining), 0)
                FROM loan_repayment_schedule
                """;
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

        Institution institution = institutionRepository.findById(institutionId).orElseThrow(() -> new InvalidRequestException("Institution does not exist"));
        BigDecimal loansOverdue = calculateTotalLoansOverdue(institutionId);
        return TotalLoansOverdueResponse.builder()
                .institutionId(institution.getId())
                .institutionName(institution.getInstitutionName())
                .totalLoansOverdue(loansOverdue)
                .build();
    }


    private BigDecimal calculateTotalLoansOverdue(String institutionId) {
        try {
            String sql = """
            SELECT COALESCE(SUM(r.balance_remaining), 0)
            FROM loan_repayment_schedule r
            JOIN loan_applications a ON a.id = r.loan_application_id
            WHERE r.due_date < CURRENT_DATE
              AND r.balance_remaining > 0
              AND a.institution_id = ?
        """;
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class, institutionId);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating total loans overdue", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public TotalInterestCollectedResponse getTotalInterestCollected(Month month, Year year) {
        log.info("Getting total interest for the month");
        String institutionId = InstitutionContext.getCurrentInstitution();

        Institution institution = institutionRepository.findById(institutionId).orElseThrow(() -> new InvalidRequestException("Institution does not exist"));
        BigDecimal interestCollected = getTotalInterest(month, year);
        return TotalInterestCollectedResponse.builder()
                .institutionId(institution.getId())
                .institutionName(institution.getInstitutionName())
                .interestCollected(interestCollected)
                .build();
    }

    private BigDecimal getTotalInterest(Month month, Year year) {
        try {
            StringBuilder sqlBuilder = new StringBuilder(
                    "SELECT COALESCE(SUM(total_interest), 0) " +
                            "FROM loan_applications WHERE 1=1 ");

            List<Object> params = new ArrayList<>();
            if (year != null) {
                sqlBuilder.append("AND EXTRACT(YEAR FROM created_at) = ? ");
                params.add(year.getValue());
            }
            if (month != null) {
                sqlBuilder.append("AND EXTRACT(MONTH FROM created_at) = ? ");
                params.add(month.getValue());
            }
            BigDecimal total = jdbcTemplate.queryForObject(
                    sqlBuilder.toString(),
                    BigDecimal.class,
                    params.toArray()
            );
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating total interest collected", e);
            return BigDecimal.ZERO;
        }
    }
}