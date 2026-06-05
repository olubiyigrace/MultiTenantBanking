package com.bank.savingsaccount;

import com.bank.memberprofiles.MemberProfile;
import com.bank.memberprofiles.MemberRepository;
import com.bank.others.auditlogs.AuditLog;
import com.bank.others.auditlogs.AuditLogRepository;
import com.bank.others.auditlogs.AuditLogRequestFilter;
import com.bank.others.config.InstitutionContext;
import com.bank.institutions.Institution;
import com.bank.others.exceptions.InvalidRequestException;
import com.bank.institutions.InstitutionRepository;
import com.bank.others.exceptions.UnauthorizedException;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SavingsServiceImpl implements SavingsService {
    private final SavingsRepository savingsRepository;
    private final JdbcTemplate jdbcTemplate;
    private final InstitutionRepository institutionRepository;
    private final AuditLogRepository auditLogRepository;
    private final CurrentUserUtil currentUserUtil;
    private final MemberRepository memberRepository;
    private final SavingsMapper savingsMapper;


    @Override
    public void createAnotherSavingsAccount(SavingsAccountRequest savingsAccountRequest) {
        User loggedInUser = currentUserUtil.getLoggedInUser();

        MemberProfile member = memberRepository.findById(loggedInUser.getMemberProfile().getId())
                .orElseThrow(() -> new InvalidRequestException("You have no member profile"));

        List<SavingsAccount> activeAccounts = savingsRepository.findByMemberIdAndSavingsStatus
                (member.getId(), SavingsStatus.ACTIVE);

        boolean hasRegular = activeAccounts.stream().anyMatch(savingsAccount ->
                savingsAccount.getSavingsAccountType() == SavingsAccountType.REGULAR);
        boolean hasFixed = activeAccounts.stream().anyMatch(savingsAccount ->
                savingsAccount.getSavingsAccountType() == SavingsAccountType.FIXED);
        boolean hasTarget = activeAccounts.stream().anyMatch(savingsAccount ->
                savingsAccount.getSavingsAccountType() == SavingsAccountType.TARGET);

        if (!hasRegular) {
            throw new UnauthorizedException(
                    "You must have an active regular savings account before creating another savings account");
        }
        if (savingsAccountRequest.getSavingsAccountType() == SavingsAccountType.REGULAR && hasRegular) {
            throw new InvalidRequestException("You already have a regular savings account");
        }
        if (savingsAccountRequest.getSavingsAccountType() == SavingsAccountType.FIXED && hasFixed) {
            throw new InvalidRequestException("You already have a fixed savings account");
        }
        if (savingsAccountRequest.getSavingsAccountType() == SavingsAccountType.TARGET && hasTarget) {
            throw new InvalidRequestException("You already have a target savings account");
        }

        SavingsAccount regularAccount = activeAccounts.stream()
                .filter(savingsAccount -> savingsAccount.getSavingsAccountType() == SavingsAccountType.REGULAR)
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("Regular savings account not found"));

        BigDecimal openingBalance = savingsAccountRequest.getBalance();
        if (openingBalance == null || openingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Opening balance must be greater than zero");
        }
        if (regularAccount.getBalance().compareTo(openingBalance) < 0) {
            throw new InvalidRequestException("Insufficient balance");
        }

        if (savingsAccountRequest.getSavingsAccountType() == SavingsAccountType.FIXED) {
            if (savingsAccountRequest.getMaturityDate() == null) {
                throw new InvalidRequestException("Maturity date is required for a fixed savings account");
            }
            if (openingBalance.compareTo(BigDecimal.valueOf(50000)) < 1) {
                throw new InvalidRequestException("Minimum opening balance for a fixed savings account is ₦50,000");
            }
        }

        if (savingsAccountRequest.getSavingsAccountType() == SavingsAccountType.TARGET) {
            if (savingsAccountRequest.getTargetAmount() == null || savingsAccountRequest.getTargetAmount()
                            .compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidRequestException("Target amount is required");
            }
            if (openingBalance.compareTo(BigDecimal.valueOf(50000)) < 1) {
                throw new InvalidRequestException("Minimum opening balance for a target savings account is ₦50,000");
            }
        }
        regularAccount.setBalance(regularAccount.getBalance().subtract(openingBalance));
        savingsRepository.save(regularAccount);

        SavingsAccount newAccount = savingsMapper.toEntity(savingsAccountRequest);
        newAccount.setMember(member);
        newAccount.setInstitution(Institution.builder().id(loggedInUser.getInstitutionId()).build());
        newAccount.setAccountNumber(generateAccountNumber());
        newAccount.setSavingsStatus(SavingsStatus.ACTIVE);
        newAccount.setBalance(openingBalance);

        if (newAccount.getSavingsAccountType() == SavingsAccountType.FIXED ||
                newAccount.getSavingsAccountType() == SavingsAccountType.TARGET) {
            newAccount.setMinimumBalance(BigDecimal.valueOf(50000));
            newAccount.setInterestRatePercent(BigDecimal.valueOf(0.027));
        }
        savingsRepository.save(newAccount);
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
                .ipAddress(AuditLogRequestFilter.CLIENT_IP.get())
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