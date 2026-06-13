package com.bank.savingsaccount;

import com.bank.loanapplications.LoanApplication;
import com.bank.loanapplications.LoanApplicationRepository;
import com.bank.loanapplications.LoanApplicationStatus;
import com.bank.loanguarantors.GuarantorRepository;
import com.bank.loanguarantors.GuarantorStatus;
import com.bank.memberprofiles.MemberProfile;
import com.bank.memberprofiles.MemberRepository;
import com.bank.memberprofiles.ProfileStatus;
import com.bank.others.auditlogs.AuditLog;
import com.bank.others.auditlogs.AuditLogRepository;
import com.bank.others.auditlogs.AuditLogRequestFilter;
import com.bank.institutions.Institution;
import com.bank.others.config.InstitutionContext;
import com.bank.others.exceptions.InvalidRequestException;
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
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SavingsServiceImpl implements SavingsService {
    private final SavingsRepository savingsRepository;
    private final JdbcTemplate jdbcTemplate;
    private final AuditLogRepository auditLogRepository;
    private final CurrentUserUtil currentUserUtil;
    private final MemberRepository memberRepository;
    private final SavingsMapper savingsMapper;
    private final LoanApplicationRepository loanApplicationRepository;
    private final GuarantorRepository guarantorRepository;


    @Override
    public void createAnotherSavingsAccount(SavingsAccountRequest savingsAccountRequest) {
        User loggedInUser = currentUserUtil.getLoggedInUser();
        String institutionId = InstitutionContext.getCurrentInstitution();

        MemberProfile member = memberRepository.findByUserIdAndInstitutionId(loggedInUser.getId(), institutionId)
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
            throw new InvalidRequestException("Insufficient balance. Deposit sufficient amount in your regular savings account");
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
        newAccount.setInstitution(member.getInstitution());
        newAccount.setAccountNumber(generateAccountNumber(loggedInUser.getInstitution()));
        newAccount.setSavingsStatus(SavingsStatus.ACTIVE);
        newAccount.setBalance(openingBalance);

        if (newAccount.getSavingsAccountType() == SavingsAccountType.FIXED ||
                newAccount.getSavingsAccountType() == SavingsAccountType.TARGET) {
            newAccount.setMinimumBalance(BigDecimal.valueOf(50000));
            newAccount.setInterestRatePercent(BigDecimal.valueOf(0.027));
        }
        savingsRepository.save(newAccount);
    }

    private String generateAccountNumber(Institution institution) {
        long count = savingsRepository.countByInstitutionId(institution.getId());
        long nextNumber = count + 1;
        return  institution.getInstitutionCode() + String.format("%06d", nextNumber);
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
        MemberProfile member = existingAccount.getMember();
        member.setProfileStatus(ProfileStatus.ACTIVE);
        memberRepository.save(member);
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
        MemberProfile member = existingAccount.getMember();
        member.setProfileStatus(ProfileStatus.SUSPENDED);
        savingsRepository.save(existingAccount);
        memberRepository.save(member);

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
        log.info("Closing savings account: {}", savingsId);

        SavingsAccount existingAccount = savingsRepository.findById(savingsId)
                .orElseThrow(() -> new InvalidRequestException("Savings account does not exist"));

        if (SavingsStatus.CLOSED.equals(existingAccount.getSavingsStatus())) {
            log.debug("Savings account {} has already been closed", savingsId);
            throw new DuplicateRequestException("Savings account has already been closed");
        }
        if (existingAccount.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidRequestException("Account balance must be zero before closure");
        }
        if (!existingAccount.getSavingsStatus().equals(SavingsStatus.ACTIVE)){
            throw new InvalidRequestException("Only an active account can be closed");
        }

        Optional<LoanApplication> activeLoan = loanApplicationRepository.findByMemberId(existingAccount.getMember().getId());
        if (activeLoan.isPresent() && !activeLoan.equals(LoanApplicationStatus.FULLY_REPAID)){
            throw new InvalidRequestException("Member has an outstanding loan");
        }

        boolean activeGuarantor = guarantorRepository.existsByGuarantorMemberIdAndGuarantorStatus(existingAccount.getMember().getId(), GuarantorStatus.ACCEPTED);
            if(activeGuarantor){
                throw new InvalidRequestException("Member is an active guarantor");
            }
        existingAccount.setSavingsStatus(SavingsStatus.CLOSED);
        MemberProfile member = existingAccount.getMember();
        member.setProfileStatus(ProfileStatus.EXITED);
        savingsRepository.save(existingAccount);
        memberRepository.save(member);

        log.info("Savings account {} closed successfully", savingsId);
    }

    @Override
    public TotalSavingsResponse getTotalSavings() {
        log.info("Getting total savings for current institution");
        User loggedInUser = currentUserUtil.getLoggedInUser();
        String institutionId = InstitutionContext.getCurrentInstitution();

        MemberProfile memberProfile = memberRepository.findByUserIdAndInstitutionId(loggedInUser.getId(), institutionId)
                .orElseThrow(() -> new InvalidRequestException("Member profile not found"));

        Institution institution = memberProfile.getInstitution();

        BigDecimal totalSavings = calculateTotalSavings(institutionId);
        return TotalSavingsResponse.builder()
                .institutionId(institutionId)
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
        User loggedInUser = currentUserUtil.getLoggedInUser();
        String institutionId = InstitutionContext.getCurrentInstitution();

        MemberProfile memberProfile = memberRepository.findByUserIdAndInstitutionId(loggedInUser.getId(), institutionId)
                .orElseThrow(() -> new InvalidRequestException("Member profile not found"));
        Institution institution = memberProfile.getInstitution();

        BigDecimal loansOutstanding = getLoansOutstanding();
        return TotalLoansOutstandingResponse.builder()
                .institutionId(institutionId)
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
        User loggedInUser = currentUserUtil.getLoggedInUser();
        String institutionId = InstitutionContext.getCurrentInstitution();

        MemberProfile memberProfile = memberRepository.findByUserIdAndInstitutionId(loggedInUser.getId(), institutionId)
                .orElseThrow(() -> new InvalidRequestException("Member profile not found"));
        Institution institution = memberProfile.getInstitution();

        BigDecimal loansOverdue = calculateTotalLoansOverdue(institutionId);
        return TotalLoansOverdueResponse.builder()
                .institutionId(institutionId)
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
        User loggedInUser = currentUserUtil.getLoggedInUser();
        String institutionId = InstitutionContext.getCurrentInstitution();

        MemberProfile memberProfile = memberRepository.findByUserIdAndInstitutionId(loggedInUser.getId(), institutionId)
                .orElseThrow(() -> new InvalidRequestException("Member profile not found"));
        Institution institution = memberProfile.getInstitution();

        BigDecimal interestCollected = getTotalInterest(month, year);
        return TotalInterestCollectedResponse.builder()
                .institutionId(institutionId)
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