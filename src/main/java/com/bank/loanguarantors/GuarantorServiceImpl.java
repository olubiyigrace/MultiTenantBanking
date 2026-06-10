package com.bank.loanguarantors;

import com.bank.loanapplications.LoanApplication;
import com.bank.loanproducts.LoanProduct;
import com.bank.others.config.InstitutionContext;
import com.bank.others.services.EmailService;
import com.bank.others.utils.CurrentUserUtil;
import com.bank.loanapplications.LoanApplicationStatus;
import com.bank.memberprofiles.ProfileStatus;
import com.bank.savingsaccount.SavingsAccount;
import com.bank.savingsaccount.SavingsAccountType;
import com.bank.savingsaccount.SavingsStatus;
import com.bank.others.exceptions.InvalidRequestException;
import com.bank.loanapplications.LoanApplicationRepository;
import com.bank.loanproducts.LoanProductRepository;
import com.bank.memberprofiles.MemberProfile;
import com.bank.memberprofiles.MemberRepository;
import com.bank.others.utils.PageResponse;
import com.bank.savingsaccount.SavingsRepository;
import com.bank.users.User;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GuarantorServiceImpl implements GuarantorService {
    private final GuarantorRepository guarantorRepository;
    private final MemberRepository memberRepository;
    private final CurrentUserUtil currentUserUtil;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanProductRepository loanProductRepository;
    private final GuarantorMapper guarantorMapper;
    private final SavingsRepository savingsRepository;
    private final EmailService emailService;


    @Override
    public void createGuarantor(GuarantorRequest guarantorRequest) throws MessagingException {
        User loggedInUser = currentUserUtil.getLoggedInUser();
        String institutionId = InstitutionContext.getCurrentInstitution();

        MemberProfile applicantProfile = memberRepository.findByUserIdAndInstitutionId(loggedInUser.getId(), institutionId)
                .orElseThrow(() -> new InvalidRequestException("Member profile not found"));

        LoanApplication loanApplication = loanApplicationRepository.findByMemberIdAndLoanApplicationStatus
                        (applicantProfile.getId(), LoanApplicationStatus.PENDING)
                .orElseThrow(() -> new InvalidRequestException("No pending loan application found"));

        LoanProduct loanProduct = loanProductRepository.findById(loanApplication.getLoanProductId()).orElseThrow(() ->
                new InvalidRequestException("Loan product does not exist"));
        if (!loanProduct.getRequiresGuarantor()) {
            throw new InvalidRequestException("This loan product does not require a guarantor");
        }

        MemberProfile guarantorMember = memberRepository.findById(guarantorRequest.getGuarantorMemberId())
                .orElseThrow(() -> new InvalidRequestException("Guarantor member does not exist"));
        if (guarantorMember.getId().equals(applicantProfile.getId())) {
            throw new InvalidRequestException("You cannot assign yourself as a guarantor");
        }

        if (!ProfileStatus.ACTIVE.equals(guarantorMember.getProfileStatus())) {
            throw new InvalidRequestException("Guarantor profile is not active");
        }

        boolean hasActiveLoan = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus
                (guarantorMember.getId(), LoanApplicationStatus.APPROVED);
        if (hasActiveLoan) {
            throw new InvalidRequestException("Guarantor already has an active loan");
        }

        boolean hasFullyRepaidLoan = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus
                (guarantorMember.getId(), LoanApplicationStatus.FULLY_REPAID);
        boolean hasAnyLoanHistory = loanApplicationRepository.existsByMemberId(guarantorMember.getId());
        if (!hasFullyRepaidLoan && hasAnyLoanHistory) {
            throw new InvalidRequestException("Guarantor must have a fully repaid loan or no loan history");
        }

        SavingsAccount fixedSavings = savingsRepository.findByMemberIdAndSavingsStatusAndSavingsAccountType(
                guarantorMember.getId(), SavingsStatus.ACTIVE, SavingsAccountType.FIXED);
        if (fixedSavings.equals(false)) {
            throw new InvalidRequestException("Guarantor must have an active fixed savings account");
        }

        BigDecimal requestedAmount = loanApplication.getRequestedAmount();
        if (fixedSavings.getBalance().compareTo(requestedAmount) < 2) {
            throw new InvalidRequestException("Guarantor savings balance is insufficient");
        }

        LocalDate loanEndDate = LocalDate.now().plusMonths(loanApplication.getTenureMonths().longValue());
        LocalDate requiredMaturityDate = loanEndDate.plusMonths(2);

        if (fixedSavings.getMaturityDate().isBefore(requiredMaturityDate)) {
            throw new InvalidRequestException("Guarantor fixed savings must mature at least 2 months after" +
                    " the applicant's loan tenure");
        }

        boolean alreadyGuarantor = guarantorRepository.existsByGuarantorMemberIdAndGuarantorStatus
                (guarantorMember.getId(), GuarantorStatus.ACCEPTED);
        boolean pendingGuarantor = guarantorRepository.existsByGuarantorMemberIdAndGuarantorStatus(
                guarantorMember.getId(), GuarantorStatus.PENDING
        );

        if (alreadyGuarantor) {
            throw new InvalidRequestException("This member is already an active guarantor");
        }
        if (pendingGuarantor) {
            throw new InvalidRequestException("This member is already a pending guarantor");
        }

        LoanGuarantor applicant = guarantorMapper.toEntity(guarantorRequest);
        applicant.setLoanApplication(LoanApplication.builder().id(loanApplication.getId()).build());
        applicant.setGuarantorStatus(GuarantorStatus.PENDING);
        guarantorRepository.save(applicant);

        Map<String, Object> model = new HashMap<>();
        model.put("name", guarantorMember.getUser().getName());
        model.put("applicantName", loggedInUser.getName());
        model.put("amount", requestedAmount);
        model.put("loanApplicationId", applicant.getLoanApplication().getId());
        model.put("institutionName", guarantorMember.getInstitution().getInstitutionName());

        emailService.sendVerificationEmail(
                guarantorMember.getUser().getEmail(),
                "Action Required: Guarantor Request for Loan Application",
                "guarantorrequest",
                model
        );
        log.info("Guarantor request created successfully");
    }

    @Override
    public void acceptGuarantorRequest(String loanApplicationId) {
        log.info("Accepting guarantor request");
        User loggedInUser = currentUserUtil.getLoggedInUser();

        LoanGuarantor existingGuarantor = guarantorRepository.findById(loggedInUser.getId()).orElseThrow(() ->
                new InvalidRequestException("You have not been requested to be a guarantor"));
        existingGuarantor.setGuarantorStatus(GuarantorStatus.ACCEPTED);
        existingGuarantor.setRespondedAt(LocalDateTime.now());

        log.info("Guarantor request accepted");
    }

    @Override
    public void rejectGuarantorRequest(String loanApplicationId) {
        log.info("Rejecting guarantor request");
        User loggedInUser = currentUserUtil.getLoggedInUser();

        LoanGuarantor existingGuarantor = guarantorRepository.findById(loggedInUser.getId()).orElseThrow(() ->
                new InvalidRequestException("You have not been requested to be a guarantor"));
        existingGuarantor.setGuarantorStatus(GuarantorStatus.REJECTED);
        existingGuarantor.setRespondedAt(LocalDateTime.now());

        log.info("Guarantor request rejected");
    }
}
