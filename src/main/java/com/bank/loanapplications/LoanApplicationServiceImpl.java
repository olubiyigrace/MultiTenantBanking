package com.bank.loanapplications;

import com.bank.loanrepaymentschedule.OverdueRepaymentScheduleResponse;
import com.bank.others.auditlogs.AuditLog;
import com.bank.others.auditlogs.AuditLogRepository;
import com.bank.loanproducts.InterestType;
import com.bank.loanrepaymentschedule.LoanRepaymentSchedule;
import com.bank.loanrepaymentschedule.LoanRepaymentStatus;
import com.bank.loanrepaymentschedule.RepaymentRepository;
import com.bank.others.auditlogs.AuditLogRequestFilter;
import com.bank.savingsaccount.SavingsAccount;
import com.bank.users.UserRepository;
import com.bank.others.utils.CurrentUserUtil;
import com.bank.others.config.InstitutionContext;
import com.bank.others.exceptions.DuplicateResourceException;
import com.bank.others.exceptions.InvalidRequestException;
import com.bank.others.exceptions.UnauthorizedException;
import com.bank.institutions.Institution;
import com.bank.loancollaterals.CollateralRepository;
import com.bank.loancollaterals.LoanCollateral;
import com.bank.loanguarantors.GuarantorRepository;
import com.bank.loanguarantors.GuarantorStatus;
import com.bank.loanguarantors.LoanGuarantor;
import com.bank.loanproducts.LoanProduct;
import com.bank.loanproducts.LoanProductRepository;
import com.bank.memberprofiles.MemberProfile;
import com.bank.memberprofiles.MemberRepository;
import com.bank.memberprofiles.ProfileStatus;
import com.bank.others.utils.PageResponse;
import com.bank.savingsaccount.SavingsRepository;
import com.bank.savingsaccount.SavingsStatus;
import com.bank.users.User;
import com.bank.users.UserAccountType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationMapper loanApplicationMapper;
    private final MemberRepository memberRepository;
    private final LoanProductRepository loanProductRepository;
    private final CurrentUserUtil currentUserUtil;
    private final UserRepository userRepository;
    private final SavingsRepository savingsRepository;
    private final CollateralRepository collateralRepository;
    private final GuarantorRepository guarantorRepository;
    private final SavingsRepository savingsAccountRepository;
    private final RepaymentRepository repaymentRepository;
    private final AuditLogRepository auditLogRepository;


    @Override
    public void createApplication(LoanApplicationRequest loanApplicationRequest) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Creating loan application");
        User user = currentUserUtil.getLoggedInUser();

        MemberProfile existingMember = memberRepository.findByUserId(user.getId()).orElseThrow(() ->
                new InvalidRequestException("Member not found"));
        if (!existingMember.getProfileStatus().equals(ProfileStatus.ACTIVE)) {
            log.debug("Member does not have an active profile status");
            throw new UnauthorizedException("Member does not have an active profile status");
        }

        boolean eligibleMember = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus
                (existingMember.getId(), LoanApplicationStatus.FULLY_REPAID);
        if (!eligibleMember) {
            throw new InvalidRequestException("You are not eligible to apply for a loan at the moment");
        }

        boolean hasSavings = savingsRepository.existsByMemberIdAndSavingsStatus
                (existingMember.getId(), SavingsStatus.ACTIVE);
        if (!hasSavings) {
            throw new InvalidRequestException("You have no active savings account at the moment");
        }

        LoanProduct existingProduct = loanProductRepository.findById(loanApplicationRequest.getLoanProductId())
                .orElseThrow(() -> new InvalidRequestException("Loan product not found"));

        if (loanApplicationRequest.getRequestedAmount().compareTo(existingProduct.getMinAmount()) < 0) {
            log.debug("Requested amount must be greater than the minimum amount");
            throw new InvalidRequestException("Requested amount must be greater than the minimum amount");
        }
        if (loanApplicationRequest.getRequestedAmount().compareTo(existingProduct.getMaxAmount()) > 0) {
            log.debug("Requested amount must not be greater than the maximum amount");
            throw new InvalidRequestException("Requested amount must not be greater than the maximum amount");
        }
        if (existingProduct.getIsActive().equals(false)) {
            log.debug("Selected loan product is not active");
            throw new InvalidRequestException("Selected loan product is not active");
        }
        if (!existingProduct.getDescription().contains(loanApplicationRequest.getPurpose())) {
            log.debug("Loan purpose does not match with the loan product description");
            throw new InvalidRequestException("Loan purpose does not match with the loan product description");
        }

        Optional<LoanGuarantor> activeLoanGuarantor = guarantorRepository.findByGuarantorMemberIdAndGuarantorStatus
                (existingMember.getId(), GuarantorStatus.ACCEPTED);
        if (activeLoanGuarantor.isPresent() && existingProduct.getRequiresCollateral().equals(false)) {
            throw new InvalidRequestException("You can only apply for a loan product that requires only a collateral");
        }
        LoanApplication loanApplication = loanApplicationMapper.toEntity(loanApplicationRequest);
        loanApplication.setInstitution(Institution.builder().id(institutionId).build());
        loanApplication.setMember(MemberProfile.builder().id(existingMember.getId()).build());
        loanApplication.setLoanApplicationStatus(LoanApplicationStatus.PENDING);
        loanApplicationRepository.save(loanApplication);
        log.info("Loan application created");
    }

    @Override
    public PageResponse<LoanApplicationResponse> getAllApplications(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        final Page<LoanApplication> loanApplications = loanApplicationRepository.findAll(pageRequest);
        final Page<LoanApplicationResponse> loanApplicationResponses = loanApplications.map(loanApplicationMapper::toResponse);
        return PageResponse.of(loanApplicationResponses);
    }

    @Override
    public void reviewLoanApplication(String loanApplicationId) {
        log.info("Reviewing loan");
        User userId = currentUserUtil.getLoggedInUser();
        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId).orElseThrow(() ->
                new InvalidRequestException("Loan Application with the id '" + loanApplicationId + "' does not exist"));

        if (loanApplication.getLoanApplicationStatus().equals(LoanApplicationStatus.UNDER_REVIEW)) {
            log.debug("Loan application is already under review");
            throw new DuplicateResourceException("Loan application is already under review");
        }
        LoanProduct loanProduct = loanProductRepository.findById(loanApplication.getLoanProductId())
                .orElseThrow(() -> new InvalidRequestException("Loan product not found"));
        LoanCollateral loanCollateral = collateralRepository.findByLoanApplicationId(loanApplicationId);
        if (loanProduct.getRequiresCollateral().equals(true) && loanCollateral.getId().isEmpty()) {
            throw new InvalidRequestException("User has not provided a collateral as required by the loan product");
        }

        LoanGuarantor loanGuarantor = guarantorRepository.findByLoanApplicationId(loanApplicationId);
        if (loanGuarantor.getGuarantorStatus().equals(GuarantorStatus.REJECTED)){
            throw new InvalidRequestException("Guarantor has been rejected ");
        }
        if (!loanGuarantor.getGuarantorStatus().equals(GuarantorStatus.ACCEPTED)){
            throw new InvalidRequestException("Guarantor is still pending ");
        }
        if (loanProduct.getRequiresGuarantor().equals(true) && loanGuarantor.getId().isBlank()) {
            throw new InvalidRequestException("User has not provided a guarantor as required by the loan product");
        }
        loanApplication.setReviewedBy(userId.getName());
        loanApplication.setReviewedAt(LocalDateTime.now());
        loanApplication.setLoanApplicationStatus(LoanApplicationStatus.UNDER_REVIEW);
        loanApplicationRepository.save(loanApplication);
        log.info("Loan reviewed");
    }

    @Override
    public void assignApplication(String loanApplicationId, String loanOfficerId) {
        log.info("Assigning loan to the loan officer");
        String institutionId = InstitutionContext.getCurrentInstitution();

        LoanApplication existingApplication = loanApplicationRepository.findById(loanApplicationId).orElseThrow(()
                -> new InvalidRequestException("Loan application does not exist"));
        if (!existingApplication.getLoanApplicationStatus().equals(LoanApplicationStatus.UNDER_REVIEW)) {
            throw new InvalidRequestException("Only loan applications with UNDER_REVIEW status can be assigned");
        }
        if (existingApplication.getLoanOfficer() != null) {
            throw new DuplicateResourceException("Loan application is already assigned to a loan officer");
        }
        User loanOfficer = userRepository.findById(loanOfficerId).orElseThrow(() ->
                new InvalidRequestException("Loan officer not found"));
        if (!loanOfficer.getUserAccountType().equals(UserAccountType.LOAN_OFFICER)) {
            throw new InvalidRequestException("User is not a loan officer");
        }
        if (!loanOfficer.getInstitution().getId().equals(institutionId)) {
            throw new UnauthorizedException("Loan officer does not belong to this institution");
        }
        existingApplication.setLoanOfficer(loanOfficer);
        loanApplicationRepository.save(existingApplication);
        log.info("Loan assigned to the loan officer");
    }

    @Override
    public PageResponse<LoanApplicationResponse> getAllAssignedApplications(int page, int size) {
        log.info("Getting all loan applications");
        User loggedInUser = currentUserUtil.getLoggedInUser();
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        final Page<LoanApplication> loanApplications = loanApplicationRepository.findByLoanOfficerId(loggedInUser.getId(), pageRequest);
        final Page<LoanApplicationResponse> loanApplicationResponses = loanApplications.map(loanApplicationMapper::toResponse);
        return PageResponse.of(loanApplicationResponses);
    }

    @Override
    public void recommendApproval(String loanApplicationId) {
        LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new InvalidRequestException("Loan application not found"));

        if (application.getRecommendationStatus() == RecommendationStatus.RECOMMENDED_APPROVAL) {
            throw new DuplicateResourceException("Loan application already recommended for approval");
        }
        if (!application.getLoanApplicationStatus().equals(LoanApplicationStatus.UNDER_REVIEW)) {
            throw new InvalidRequestException("Only loan applications under review can be given recommendation");
        }
        application.setRecommendationStatus(RecommendationStatus.RECOMMENDED_APPROVAL);
        loanApplicationRepository.save(application);
    }

    @Override
    public void approveLoan(String loanApplicationId) {
        log.info("Approving loan application");
        LoanApplication existingApplication = loanApplicationRepository.findById(loanApplicationId).orElseThrow(() ->
                new InvalidRequestException("Loan application with the id '" + loanApplicationId + "' does not exist"));
        if (existingApplication.getLoanApplicationStatus() == LoanApplicationStatus.APPROVED) {
            throw new DuplicateResourceException("Loan application has already been approved");
        }
        if (!existingApplication.getLoanApplicationStatus().equals(LoanApplicationStatus.UNDER_REVIEW)) {
            throw new InvalidRequestException("Loan application has not been reviewed");
        }
        existingApplication.setLoanApplicationStatus(LoanApplicationStatus.APPROVED);
        existingApplication.setApprovedAmount(existingApplication.getRequestedAmount());
        loanApplicationRepository.save(existingApplication);
        log.info("Loan approved");
    }

    public void recommendRejection(String loanApplicationId) {
        LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new InvalidRequestException("Loan application not found"));

        if (application.getRecommendationStatus() == RecommendationStatus.RECOMMENDED_REJECTION) {
            throw new DuplicateResourceException("Already recommended for rejection");
        }
        if (application.getLoanApplicationStatus() != LoanApplicationStatus.UNDER_REVIEW) {
            throw new InvalidRequestException("Only loan applications under review can be given recommendation");
        }

        application.setRecommendationStatus(RecommendationStatus.RECOMMENDED_REJECTION);
        loanApplicationRepository.save(application);
    }

    @Override
    public void rejectLoan(String loanApplicationId, LoanRejectionRequest loanRejectionRequest) {
        log.info("Rejecting loan application");
        LoanApplication existingApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new InvalidRequestException("Loan application with id '" + loanApplicationId + "' does not exist"));
        if (existingApplication.getLoanApplicationStatus() == LoanApplicationStatus.REJECTED) {
            throw new DuplicateResourceException("Loan application has already been rejected");
        }
        if (existingApplication.getLoanApplicationStatus() != LoanApplicationStatus.UNDER_REVIEW) {
            throw new InvalidRequestException("Only loan applications under review can be rejected");
        }

        LocalDateTime createdAt = existingApplication.getCreatedAt();
        if (createdAt != null && createdAt.plusDays(7).isAfter(LocalDateTime.now())) {
            throw new InvalidRequestException("Loan application cannot be rejected before 7 days from creation date");
        }
        existingApplication.setLoanApplicationStatus(LoanApplicationStatus.REJECTED);
        existingApplication.setRejectionReason(loanRejectionRequest.getLoanRejectionReason());
        loanApplicationRepository.save(existingApplication);
        log.info("Loan application {} rejected successfully", loanApplicationId);
    }

    @Override
    public void disburseLoan(String loanApplicationId) {
        log.info("Disbursing loan");

        LoanApplication existingApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() ->
                        new InvalidRequestException("Loan application with id '" + loanApplicationId + "' does not exist"));

        LoanProduct loanProduct = loanProductRepository.findById(existingApplication.getLoanProductId())
                .orElseThrow(() -> new InvalidRequestException("Loan product not found"));

        if (existingApplication.getLoanApplicationStatus() == LoanApplicationStatus.DISBURSED) {
            throw new DuplicateResourceException("Loan application has already been disbursed");
        }
        if (existingApplication.getLoanApplicationStatus() != LoanApplicationStatus.APPROVED) {
            throw new InvalidRequestException("Loan application has not been approved");
        }

        BigDecimal principal = existingApplication.getApprovedAmount();
        BigDecimal rate = loanProduct.getInterestRatePercent();
        BigDecimal tenure = loanProduct.getMaxTenureMonths();

        existingApplication.setTenureMonths(tenure);
        existingApplication.setInterestRatePercent(rate);
        existingApplication.setInterestType(loanProduct.getInterestType());

        BigDecimal processingFee = loanProduct.getProcessingFeePercent().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP).multiply(principal);
        existingApplication.setProcessingFee(processingFee);

        BigDecimal totalInterest;
        BigDecimal monthlyInstallment;
        BigDecimal totalRepayable;

        if (loanProduct.getInterestType() == InterestType.FLAT) {
            totalInterest = principal.multiply(rate).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP).multiply(tenure);
            totalRepayable = principal.add(totalInterest);
            monthlyInstallment = totalRepayable.divide(tenure, 2, RoundingMode.HALF_UP);
        }
        else if (loanProduct.getInterestType() == InterestType.REDUCING_BALANCE) {
            int n = tenure.intValueExact();
            BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
            BigDecimal onePlusRPowerN = (BigDecimal.ONE.add(monthlyRate)).pow(n);
            monthlyInstallment = principal.multiply(monthlyRate).multiply(onePlusRPowerN)
                    .divide(onePlusRPowerN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
            totalRepayable = monthlyInstallment.multiply(tenure);
            totalInterest = totalRepayable.subtract(principal);
        }
        else {
            throw new InvalidRequestException("Unsupported interest type");
        }
        BigDecimal netDisbursement = principal.subtract(processingFee);

        existingApplication.setTotalInterest(totalInterest);
        existingApplication.setNetDisbursement(netDisbursement);
        existingApplication.setTotalRepayable(totalRepayable);
        existingApplication.setMonthlyInstallment(monthlyInstallment);
        existingApplication.setLoanApplicationStatus(LoanApplicationStatus.DISBURSED);
        existingApplication.setDisbursedAt(LocalDateTime.now());

        SavingsAccount savingsAccount = savingsAccountRepository.findByMemberId(existingApplication.getMember().getId())
                .orElseThrow(() -> new InvalidRequestException("Savings account not found"));
        savingsAccount.setBalance(savingsAccount.getBalance().add(netDisbursement));

        savingsAccountRepository.save(savingsAccount);
        loanApplicationRepository.save(existingApplication);
        generateRepaymentSchedule(existingApplication,principal,totalInterest,tenure);

        log.info("Loan disbursed successfully");
    }

    private void generateRepaymentSchedule(LoanApplication loanApplication, BigDecimal principal, BigDecimal totalInterest, BigDecimal tenure) {
        int installments = tenure.intValueExact();
        BigDecimal principalDue = principal.divide(tenure, 2, RoundingMode.HALF_UP);
        BigDecimal interestDue = totalInterest.divide(tenure, 2, RoundingMode.HALF_UP);
        BigDecimal totalDue = principalDue.add(interestDue);

        List<LoanRepaymentSchedule> schedules = new ArrayList<>();
        for (int i = 1; i <= installments; i++) {
            String installmentNumber = String.format("P%03d", i);

            LoanRepaymentSchedule schedule = LoanRepaymentSchedule.builder()
                    .installmentNumber(installmentNumber)
                    .principalDue(principalDue)
                    .interestDue(interestDue)
                    .totalDue(totalDue)
                    .amountPaid(BigDecimal.ZERO)
                    .balanceRemaining(totalDue)
                    .dueDate(LocalDate.now().plusMonths(i))
                    .loanRepaymentStatus(LoanRepaymentStatus.PENDING)
                    .loanApplication(loanApplication)
                    .build();
            schedules.add(schedule);
        }
        repaymentRepository.saveAll(schedules);
    }


    @Override
    public void checkIfRepaid(String loanApplicationId) {
// savings account is not active for guarantor
//                    .paidAt(LocalDateTime.now()) set this to null and in repaymentservice, set it to now and status to paid in the repaid method
//if loanrepayment is < totalrefundable, set loan repayment schedule to partial
        //if loan repayment schedule status is paid
//        //if all loans are paid i.e installments
//        loanApplication.setLoanApplicationStatus(LoanApplicationStatus.FULLY_REPAID);
//        loanApplication.setRequiresCollateral(null);
//        loanApplication.setRequiresGuarantor(null);
//        amountPaid += payment
//        balanceRemaining = totalDue - amountPaid
    }

    @Override
    public void addDefaulter(String loanApplicationId) {
//        loanApplication.setLoanApplicationStatus(LoanApplicationStatus.FULLY_REPAID);
        // if loan due date is before local date time.now, set status as overdue
 // if loan repayment schedule is overdue, add as defaulter
    }


    @Override
    public void writeOff(String loanApplicationId) {
        User currentUser = currentUserUtil.getLoggedInUser();
        log.info("Writing off a defaulted loan");

        LoanApplication existingApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new InvalidRequestException("Loan application with id '" + loanApplicationId + "' does not exist"));

        LoanApplicationStatus oldStatus = existingApplication.getLoanApplicationStatus();
        if (existingApplication.getLoanApplicationStatus() == LoanApplicationStatus.WRITTEN_OFF) {
            throw new DuplicateResourceException("Loan application has already been written off");
        }
        if (existingApplication.getLoanApplicationStatus() != LoanApplicationStatus.DEFAULTED) {
            throw new InvalidRequestException("Only defaulted loans can be written off");
        }

        LoanRepaymentSchedule repaymentSchedule = repaymentRepository.findTopByLoanApplicationIdOrderByDueDateAsc(
                existingApplication.getId()).orElseThrow(() -> new InvalidRequestException
                ("No repayment schedule found for this loan"));

        long daysPastDue = ChronoUnit.DAYS.between(repaymentSchedule.getDueDate(), LocalDate.now());
        if (daysPastDue < 180) {
            throw new InvalidRequestException("Loan cannot be written off until it is at least 180 days past due");
        }
        existingApplication.setLoanApplicationStatus(LoanApplicationStatus.WRITTEN_OFF);
        loanApplicationRepository.save(existingApplication);

        AuditLog auditLog = AuditLog.builder()
                .institution(existingApplication.getInstitution())
                .user(currentUser)
                .entityType("LOAN_APPLICATION")
                .action("WRITE_OFF")
                .entityId(existingApplication.getId())
                .oldValue(oldStatus.name())
                .ipAddress(AuditLogRequestFilter.CLIENT_IP.get())
                .newValue(LoanApplicationStatus.WRITTEN_OFF.name())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Loan application {} written off successfully", loanApplicationId);
    }

    @Override
    public PageResponse<OverdueRepaymentScheduleResponse> getOverdueRepaymentSchedules(int page, int size) {
        User currentUser = currentUserUtil.getLoggedInUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        Page<LoanRepaymentSchedule> schedules = repaymentRepository.findOverdueSchedulesByLoanOfficer(currentUser.getId(), pageRequest);
        Page<OverdueRepaymentScheduleResponse> content = schedules.map(this::mapToResponse);
        return PageResponse.of(content);
    }

    private OverdueRepaymentScheduleResponse mapToResponse(LoanRepaymentSchedule schedule) {
        LoanApplication loan = schedule.getLoanApplication();
        return OverdueRepaymentScheduleResponse.builder()
                .repaymentScheduleId(schedule.getId())
                .loanApplicationId(loan.getId())
                .memberName(loan.getMember().getUser().getName())
                .installmentAmount(schedule.getTotalDue())
                .amountPaid(schedule.getAmountPaid())
                .balanceRemaining(schedule.getBalanceRemaining())
                .dueDate(schedule.getDueDate())
                .repaymentStatus(schedule.getLoanRepaymentStatus())
                .build();
    }
}

