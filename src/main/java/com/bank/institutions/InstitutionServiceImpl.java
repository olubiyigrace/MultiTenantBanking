package com.bank.institutions;

import com.bank.loanapplications.TotalLoansDisbursedResponse;
import com.bank.loanapplications.TotalLoansDisbursedStatisticsResponse;
import com.bank.loanrepaymentschedule.TotalLoansOutstandingResponse;
import com.bank.loanrepaymentschedule.TotalLoansOutstandingStatisticsResponse;
import com.bank.memberprofiles.TotalMemberResponse;
import com.bank.memberprofiles.TotalMembersStatisticsResponse;
import com.bank.others.utils.CurrentUserUtil;
import com.bank.savingsaccount.TotalSavingsResponse;
import com.bank.savingsaccount.TotalSavingsStatisticsResponse;
import com.bank.transactions.TotalDepositsResponse;
import com.bank.transactions.TotalDepositsStatisticsResponse;
import com.bank.users.UserMapper;
import com.bank.users.UserResponse;
import com.bank.others.utils.PageResponse;
import com.bank.users.User;
import com.bank.users.UserAccountType;
import com.bank.users.UserRepository;
import com.bank.others.exceptions.DuplicateResourceException;
import com.bank.others.exceptions.InvalidRequestException;
import com.bank.others.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class InstitutionServiceImpl implements InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final InstitutionMapper institutionMapper;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final CurrentUserUtil currentUserUtil;

    @Override
    public void approveInstitution(final String institutionId) throws MessagingException {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getIsVerified() != true){
            throw new InvalidRequestException("Institution has not been verified");
        }
        institution.setInstitutionStatus(InstitutionStatus.ACTIVE);
        institutionRepository.save(institution);
        try {
            createAdminUser(institution);
        } catch (final Exception e) {
            log.error("Institution approval failed", e);
            rollBackInstitutionStatus(institution);
            throw e;
        }
    }

    private void createAdminUser(Institution institution) throws MessagingException {
        if (userRepository.existsByUsername(institution.getAdminUsername())) {
            log.debug("User already exists");
            throw new DuplicateResourceException("User already exists");
        }
        final User adminUser = User.builder()
                .username(institution.getAdminUsername())
                .email(institution.getAdminEmail())
                .name(institution.getAdminName())
                .nin(institution.getAdminNin())
                .phone(institution.getAdminPhone())
                .password(institution.getAdminPassword())
                .isVerified(false)
                .institution(institution)
                .userAccountType(UserAccountType.INSTITUTION_ADMIN)
                .build();

        String emailVerificationToken = UUID.randomUUID().toString();
        adminUser.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
        adminUser.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(adminUser);

        log.info("Admin user created successfully");

        Map<String, Object> model = new HashMap<>();
        model.put("name", institution.getAdminName());
        model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/verify"
                + "?token=" + emailVerificationToken
             );

        emailService.sendVerificationEmail(
                institution.getAdminEmail(),
                "Verify your account",
                "userverification",
                model
        );
    }

    @Override
    public void activateInstitution(String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.SUSPENDED) {
            log.debug("Institution was not suspended");
            throw new InvalidRequestException("Institution was not suspended");
        }
        institution.setInstitutionStatus(InstitutionStatus.ACTIVE);
        institutionRepository.save(institution);
    }

    @Override
    public void suspendInstitution(String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.ACTIVE) {
            throw new InvalidRequestException("Institution is pending");
        }
        institution.setInstitutionStatus(InstitutionStatus.SUSPENDED);
        institutionRepository.save(institution);
    }

    @Override
    public PageResponse<InstitutionResponse> findAllInstitution(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Institution> institutions = institutionRepository.findAll(pageRequest);
        final Page<InstitutionResponse> institutionResponse = institutions.map(institutionMapper::toResponse);
        return PageResponse.of(institutionResponse);
    }

    private void rollBackInstitutionStatus(Institution institution) {
        institution.setInstitutionStatus(InstitutionStatus.PENDING);
        institutionRepository.save(institution);
        log.debug("Institution not approved");
    }


    @Override
    public TotalMembersStatisticsResponse getMembersStatistics() {
        List<Institution> institutions = institutionRepository.findAll();
        List<TotalMemberResponse> perInstitution = new ArrayList<>();
        long totalMembersAcrossAll = 0L;

        for (Institution institution : institutions) {
            Long members = countMembers(institution.getId());
            totalMembersAcrossAll += members;
            perInstitution.add(
                    TotalMemberResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(institution.getInstitutionName())
                            .totalMembers(members)
                            .build());
        }
        return TotalMembersStatisticsResponse.builder()
                .totalInstitutionMembers(totalMembersAcrossAll)
                .institutions(perInstitution)
                .build();
    }

    private Long countMembers(String institutionId) {
        String sql = "SELECT COUNT(*) FROM member_profiles WHERE institution_id = ? ";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, institutionId);
        return count != null ? count : 0L;
    }

    @Override
    public TotalSavingsStatisticsResponse getSavingsStatistics() {
        List<Institution> institutions = institutionRepository.findAll();
        List<TotalSavingsResponse> perInstitution = new ArrayList<>();
        BigDecimal totalSavingsAcrossAll = BigDecimal.ZERO;

        for (Institution institution : institutions) {
            BigDecimal savings = getInstitutionSavings(institution.getId());
            totalSavingsAcrossAll = totalSavingsAcrossAll.add(savings);

            perInstitution.add(
                    TotalSavingsResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(institution.getInstitutionName())
                            .totalSavingsBalance(savings)
                            .build());
        }
        return TotalSavingsStatisticsResponse.builder()
                .totalInstitutionsSavingsBalance(totalSavingsAcrossAll)
                .institutions(perInstitution)
                .build();
    }

    private BigDecimal getInstitutionSavings(String institutionId) {
        try {
            String sql = "SELECT COALESCE(SUM(balance), 0) FROM savings_accounts WHERE institution_id = ?";
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class, institutionId);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating savings for institution {}", institutionId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public TotalLoansOutstandingStatisticsResponse getLoansOutstandingStatistics() {
        List<Institution> institutions = institutionRepository.findAll();
        Map<String, BigDecimal> outstandingByInstitution = getOutstandingLoansByInstitution();
        List<TotalLoansOutstandingResponse> perInstitution = new ArrayList<>();
        BigDecimal totalLoansOutstandingAcrossAll = BigDecimal.ZERO;

        for (Institution institution : institutions) {
            BigDecimal outstanding = outstandingByInstitution.getOrDefault(institution.getId(), BigDecimal.ZERO);
            totalLoansOutstandingAcrossAll = totalLoansOutstandingAcrossAll.add(outstanding);

            perInstitution.add(
                    TotalLoansOutstandingResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(institution.getInstitutionName())
                            .totalLoansOutstanding(outstanding)
                            .build());
        }
        return TotalLoansOutstandingStatisticsResponse.builder()
                .totalInstitutionsLoansOutstanding(totalLoansOutstandingAcrossAll)
                .institutions(perInstitution)
                .build();
    }

    private Map<String, BigDecimal> getOutstandingLoansByInstitution() {
        String sql = """
            SELECT la.institution_id,
                   COALESCE(SUM(lrs.balance_remaining), 0) AS total_outstanding
            FROM loan_repayment_schedule lrs
            JOIN loan_applications la
                ON la.id = lrs.loan_application_id
            GROUP BY la.institution_id
            """;

        return jdbcTemplate.query(sql, rs -> {
            Map<String, BigDecimal> result = new HashMap<>();

            while (rs.next()) {
                result.put(
                        rs.getString("institution_id"),
                        rs.getBigDecimal("total_outstanding"));
            }
            return result;
        });
    }

    @Override
    public TotalDepositsStatisticsResponse getDepositsStatistics() {
        List<Institution> institutions = institutionRepository.findAll();
        Map<String, BigDecimal> depositsByInstitution = getDepositsByInstitution();
        List<TotalDepositsResponse> perInstitution = new ArrayList<>();
        BigDecimal totalDepositsAcrossAll = BigDecimal.ZERO;

        for (Institution institution : institutions) {
            BigDecimal deposits = depositsByInstitution.getOrDefault(institution.getId(), BigDecimal.ZERO);
            totalDepositsAcrossAll = totalDepositsAcrossAll.add(deposits);

            perInstitution.add(
                    TotalDepositsResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(institution.getInstitutionName())
                            .totalDeposits(deposits)
                            .build());
        }
        return TotalDepositsStatisticsResponse.builder()
                .totalInstitutionsDeposits(totalDepositsAcrossAll)
                .institutions(perInstitution)
                .build();
    }

    private Map<String, BigDecimal> getDepositsByInstitution() {
        String sql = """
            SELECT institution_id,
                   COALESCE(SUM(amount), 0) AS total_deposits
            FROM transactions
            WHERE transaction_type = 'DEPOSIT'
            GROUP BY institution_id
            """;
        return jdbcTemplate.query(sql, rs -> {
            Map<String, BigDecimal> result = new HashMap<>();

            while (rs.next()) {
                result.put(rs.getString("institution_id"), rs.getBigDecimal("total_deposits"));
            }
            return result;
        });
    }

    @Override
    public TotalLoansDisbursedStatisticsResponse getLoansDisbursedStatistics(Month month, Year year) {
        List<Institution> institutions = institutionRepository.findAll();
        Map<String, BigDecimal> disbursedByInstitution = getLoansDisbursedByInstitution(month, year);
        List<TotalLoansDisbursedResponse> perInstitution = new ArrayList<>();
        BigDecimal totalLoansDisbursedAcrossAll = BigDecimal.ZERO;

        for (Institution institution : institutions) {
            BigDecimal disbursed = disbursedByInstitution.getOrDefault(institution.getId(), BigDecimal.ZERO);
            totalLoansDisbursedAcrossAll = totalLoansDisbursedAcrossAll.add(disbursed);
            perInstitution.add(
                    TotalLoansDisbursedResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(institution.getInstitutionName())
                            .totalLoansDisbursed(disbursed)
                            .build());
        }
        return TotalLoansDisbursedStatisticsResponse.builder()
                .totalInstitutionsLoansDisbursed(totalLoansDisbursedAcrossAll)
                .institutions(perInstitution)
                .build();
    }

    private Map<String, BigDecimal> getLoansDisbursedByInstitution(Month month, Year year) {
        StringBuilder sql = new StringBuilder("""
            SELECT institution_id, COALESCE(SUM(net_disbursement), 0) AS total_disbursed
            FROM loan_applications
            WHERE loan_application_status = 'DISBURSED'
            """);

        List<Object> params = new ArrayList<>();
        if (year != null) {
            sql.append(" AND EXTRACT(YEAR FROM disbursed_at) = ?");
            params.add(year.getValue());
        }
        if (month != null) {
            sql.append(" AND EXTRACT(MONTH FROM disbursed_at) = ?");
            params.add(month.getValue());
        }
        sql.append(" GROUP BY institution_id");

        return jdbcTemplate.query(sql.toString(), rs -> {
            Map<String, BigDecimal> result = new HashMap<>();
            while (rs.next()) {
                result.put(
                        rs.getString("institution_id"),
                        rs.getBigDecimal("total_disbursed"));
            }
            return result;
        }, params.toArray());
    }
}

