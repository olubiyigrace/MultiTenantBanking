package com.bank.services.impl;

import com.bank.common.PageResponse;
import com.bank.entities.User;
import com.bank.enums.UserAccountType;
import com.bank.repositories.UserRepository;
import com.bank.entities.Institution;
import com.bank.enums.InstitutionStatus;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.InvalidRequestException;
import com.bank.repositories.InstitutionRepository;
import com.bank.responses.*;
import com.bank.services.InstitutionService;
import com.bank.mapper.InstitutionMapper;
import com.bank.services.ProvisioningService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final InstitutionMapper institutionMapper;
    private final ProvisioningService provisioningService;
    private final JdbcTemplate jdbcTemplate;


    @Override
    public void approveInstitution(final String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        institutionRepository.save(institution);
        try {
            provisioningService.provisionInstitution(institution);
            createAdminUser(institution);
        } catch (final Exception e) {
            log.error("Institution approval failed", e);
            rollBackInstitutionStatus(institution);
            throw e;
        }
    }

    @Override
    public void activateInstitution(String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.PENDING) {
            log.debug("Institution is not pending");
            throw new InvalidRequestException("Institution is not pending");
        }
        institution.setInstitutionStatus(InstitutionStatus.ACTIVE);
        institutionRepository.save(institution);
    }

    @Override
    public void deactivateInstitution(String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.ACTIVE) {
            log.debug("Institution is pending");
            throw new InvalidRequestException("Institution is pending");
        }
        institution.setInstitutionStatus(InstitutionStatus.INACTIVE);
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

    private void createAdminUser(Institution institution) {
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
                .userAccountType(UserAccountType.INSTITUTION_ADMIN)
                .institution(institution)
                .build();
        userRepository.save(adminUser);
        log.info("Admin user created successfully");
    }

    @Override
    public TotalMembersStatisticsResponse getMembersStatistics() {
        List<Institution> institutions = institutionRepository.findAll();
        List<TotalMemberResponse> perInstitution = new ArrayList<>();
        long totalMembersAcrossAll = 0L;

        for (Institution institution : institutions) {
            String schema = institution.getInstitutionName().toLowerCase();
            Long members = countMembers(schema);
            totalMembersAcrossAll += members;

            perInstitution.add(
                    TotalMemberResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(schema)
                            .totalMembers(members)
                            .build());
        }
        return TotalMembersStatisticsResponse.builder()
                .totalInstitutionMembers(totalMembersAcrossAll)
                .institutions(perInstitution)
                .build();
    }

    private Long countMembers(String schema) {
        try {
            String sql = """
            SELECT COUNT(*)
            FROM %s.member_profiles
            """.formatted(schema);

            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0L;
        }catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public TotalSavingsStatisticsResponse getSavingsStatistics() {
        List<Institution> institutions = institutionRepository.findAll();
        List<TotalSavingsResponse> perInstitution = new ArrayList<>();
        BigDecimal totalSavingsAcrossAll = BigDecimal.ZERO;

        for (Institution institution : institutions) {
            String schema = institution.getInstitutionName().toLowerCase();
            BigDecimal savings = getInstitutionSavings(schema);
            totalSavingsAcrossAll = totalSavingsAcrossAll.add(savings);

            perInstitution.add(
                    TotalSavingsResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(schema)
                            .totalSavingsBalance(savings)
                            .build()
            );
        }
        return TotalSavingsStatisticsResponse.builder()
                .totalInstitutionsSavingsBalance(totalSavingsAcrossAll)
                .institutions(perInstitution)
                .build();
    }

    private BigDecimal getInstitutionSavings(String schema) {
        try {
            String sql = """
            SELECT COALESCE(SUM(balance), 0)
            FROM %s.savings_accounts
            """.formatted(schema);
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        }
        catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public TotalLoansOutstandingStatisticsResponse getLoansOutstandingStatistics() {
        List<Institution> institutions = institutionRepository.findAll();
        List<TotalLoansOutstandingResponse> perInstitution = new ArrayList<>();
        BigDecimal totalLoansOutstandingAcrossAll = BigDecimal.ZERO;

        for (Institution institution : institutions) {
            String schema = institution.getInstitutionName().toLowerCase();
            BigDecimal loansOutstanding = getInstitutionLoansOutstanding(schema);
            totalLoansOutstandingAcrossAll = totalLoansOutstandingAcrossAll.add(loansOutstanding);

            perInstitution.add(
                    TotalLoansOutstandingResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(schema)
                            .totalLoansOutstanding(loansOutstanding)
                            .build()
            );
        }
        return TotalLoansOutstandingStatisticsResponse.builder()
                .totalInstitutionsLoansOutstanding(totalLoansOutstandingAcrossAll)
                .institutions(perInstitution)
                .build();
    }

    private BigDecimal getInstitutionLoansOutstanding(String schema) {
        try {
            String sql = """
            SELECT COALESCE(SUM(balance_remaining), 0)
            FROM %s.loan_repayment_schedule
            """.formatted(schema);
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        }
        catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public TotalDepositsStatisticsResponse getDepositsStatistics() {
        List<Institution> institutions = institutionRepository.findAll();
        List<TotalDepositsResponse> perInstitution = new ArrayList<>();
        BigDecimal totalDepositsAcrossAll = BigDecimal.ZERO;

        for (Institution institution : institutions) {
            String schema = institution.getInstitutionName().toLowerCase();
            BigDecimal deposits = getInstitutionDeposits(schema);
            totalDepositsAcrossAll = totalDepositsAcrossAll.add(deposits);

            perInstitution.add(
                    TotalDepositsResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(schema)
                            .totalDeposits(deposits)
                            .build()
            );
        }

        return TotalDepositsStatisticsResponse.builder()
                .totalInstitutionsDeposits(totalDepositsAcrossAll)
                .institutions(perInstitution)
                .build();
    }

    private BigDecimal getInstitutionDeposits(String schema) {
        try {
            String sql = """
            SELECT COALESCE(SUM(transaction_type.DEPOSIT), 0)
            FROM %s.transactions
            """.formatted(schema);
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        }
        catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public TotalLoansDisbursedStatisticsResponse getLoansDisbursedStatistics(java.time.Month month, java.time.Year year) {
        List<Institution> institutions = institutionRepository.findAll();
        List<TotalLoansDisbursedResponse> perInstitution = new ArrayList<>();
        BigDecimal totalLoansDisbursedAcrossAll = BigDecimal.ZERO;

        for (Institution institution : institutions) {
            String schema = institution.getInstitutionName().toLowerCase();
            BigDecimal loansDisbursed = getInstitutionLoansDisbursed(schema, month, year);
            totalLoansDisbursedAcrossAll = totalLoansDisbursedAcrossAll.add(loansDisbursed);

            perInstitution.add(
                    TotalLoansDisbursedResponse.builder()
                            .institutionId(institution.getId())
                            .institutionName(schema)
                            .totalLoansDisbursed(loansDisbursed)
                            .build()
            );
        }
        return TotalLoansDisbursedStatisticsResponse.builder()
                .totalInstitutionsLoansDisbursed(totalLoansDisbursedAcrossAll)
                .institutions(perInstitution)
                .build();
    }

    private BigDecimal getInstitutionLoansDisbursed(String schema, java.time.Month month, java.time.Year year) {
        try {
            StringBuilder sqlBuilder = new StringBuilder(
                    "SELECT COALESCE(SUM(approved_amount), 0) FROM %s.loan_applications WHERE 1=1 ".formatted(schema)
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
        }
        catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}

