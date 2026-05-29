package com.bank.services.impl;

import com.bank.auth.repository.UserRepository;
import com.bank.auth.util.CurrentUserUtil;
import com.bank.config.InstitutionContext;
import com.bank.entities.*;
import com.bank.enums.LoanApplicationStatus;
import com.bank.enums.UserAccountType;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.InvalidRequestException;
import com.bank.exceptions.UnauthorizedException;
import com.bank.mapper.LoanApplicationMapper;
import com.bank.repositories.LoanApplicationRepository;
import com.bank.repositories.LoanProductRepository;
import com.bank.repositories.MemberRepository;
import com.bank.requests.LoanApplicationRequest;
import com.bank.responses.LoanApplicationResponse;
import com.bank.responses.PageResponse;
import com.bank.services.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@Slf4j
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationMapper loanApplicationMapper;
    private final MemberRepository memberRepository;
    private final LoanProductRepository loanProductRepository;
    private final CurrentUserUtil currentUserUtil;
    private final UserRepository userRepository;


    @Override
    public void createApplication(LoanApplicationRequest loanApplicationRequest) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Creating loan application");

        User user = currentUserUtil.getLoggedInUser();

        MemberProfile existingMember = memberRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidRequestException("Member not found"));

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
        boolean approved = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus(existingMember.getId(), LoanApplicationStatus.APPROVED);
        if (approved) {
            log.debug("You have an approved loan");
            throw new DuplicateResourceException("You have an approved loan");
        }
        boolean pending = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus(existingMember.getId(), LoanApplicationStatus.PENDING);
        if (pending) {
            log.debug("You have a pending loan application");
            throw new DuplicateResourceException("You have a pending loan application");
        }
        boolean defaulted = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus(existingMember.getId(), LoanApplicationStatus.DEFAULTED);
        if (defaulted) {
            log.debug("Cannot apply for a loan at the moment. Pay your outstanding loan");
            throw new DuplicateResourceException("Cannot apply for a loan at the moment. Pay your outstanding loan");
        }
        boolean disbursed = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus(existingMember.getId(), LoanApplicationStatus.DISBURSED);
        if (disbursed) {
            log.debug("You have an active loan. Try again after repayment");
            throw new DuplicateResourceException("You have an active loan. Try again after repayment");
        }
        boolean underReview = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus(existingMember.getId(), LoanApplicationStatus.UNDER_REVIEW);
        if (underReview) {
            log.debug("Your previous application is still under review");
            throw new DuplicateResourceException("Your previous application is still under review");
        }
        boolean writtenOff = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus(existingMember.getId(), LoanApplicationStatus.WRITTEN_OFF);
        if (writtenOff) {
            log.debug("Cannot apply for a loan.");
            throw new UnauthorizedException("Cannot apply for a loan.");
        }
        boolean rejected = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus(existingMember.getId(), LoanApplicationStatus.REJECTED);
        if (rejected) {
            log.debug("Rejected!");
            throw new UnauthorizedException("Rejected");
        }

        LoanApplication loanApplication = loanApplicationMapper.toEntity(loanApplicationRequest);
        loanApplication.setInstitution(Institution.builder().id(institutionId).build());
        loanApplication.setMember(MemberProfile.builder().id(existingMember.getId()).build());
        loanApplication.setLoanApplicationStatus(LoanApplicationStatus.PENDING);
        loanApplicationRepository.save(loanApplication);
    }

    @Override
    public PageResponse<LoanApplicationResponse> getAllApplications(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        final Page<LoanApplication> loanApplications = loanApplicationRepository.findAll(pageRequest);
        final Page<LoanApplicationResponse> loanApplicationResponses = loanApplications.map(loanApplicationMapper::toResponse);
        return PageResponse.of(loanApplicationResponses);
    }

    @Override
    public void assignApplication(String loanApplicationId, String loanOfficerId) {
        String institutionId = InstitutionContext.getCurrentInstitution();

        LoanApplication existingApplication = loanApplicationRepository.findById(loanApplicationId).orElseThrow(() -> new InvalidRequestException("Loan application does not exist"));
        if (!existingApplication.getLoanApplicationStatus().equals(LoanApplicationStatus.PENDING)) {
            throw new InvalidRequestException("Only pending loan applications can be assigned");
        }
        User loanOfficer = userRepository.findById(loanOfficerId).orElseThrow(() -> new InvalidRequestException("Loan officer not found"));
        if (!loanOfficer.getUserAccountType().equals(UserAccountType.LOAN_OFFICER)) {
            throw new InvalidRequestException("User is not a loan officer");
        }
        if (!loanOfficer.getInstitution().getId().equals(institutionId)) {
            throw new UnauthorizedException("Loan officer does not belong to this institution");
        }
        existingApplication.setLoanOfficer(loanOfficer);
        loanApplicationRepository.save(existingApplication);
    }

    @Override
    public PageResponse<LoanApplicationResponse> getAllAssignedApplications(int page, int size) {
        User loggedInUser = currentUserUtil.getLoggedInUser();
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        final Page<LoanApplication> loanApplications = loanApplicationRepository.findByLoanOfficerId(loggedInUser.getId(), pageRequest);
        final Page<LoanApplicationResponse> loanApplicationResponses = loanApplications.map(loanApplicationMapper::toResponse);
        return PageResponse.of(loanApplicationResponses);
    }

    @Override
    public void reviewLoanApplication(String loanApplicationId) {
        User userId = currentUserUtil.getLoggedInUser();
        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId). orElseThrow(() ->
            new InvalidRequestException("Loan Application with the id '" + loanApplicationId + "' does not exist"));

        if (loanApplication.getLoanApplicationStatus().equals(LoanApplicationStatus.UNDER_REVIEW)) {
            log.debug("Loan application is already under review");
            throw new DuplicateResourceException("Loan application is already under review");
        }
        loanApplication.setReviewedBy(userId.getName());
        loanApplication.setReviewedAt(LocalDateTime.now());
        loanApplication.setLoanApplicationStatus(LoanApplicationStatus.UNDER_REVIEW);
        loanApplicationRepository.save(loanApplication);
    }

    @Override
    public void approveLoan(String loanApplicationId) {
        LoanApplication existingApplication = loanApplicationRepository.findById(loanApplicationId).orElseThrow(() ->
                new InvalidRequestException("Loan application with the id '" + loanApplicationId + "' does not exist"));
        if(!existingApplication.getLoanApplicationStatus().equals(LoanApplicationStatus.UNDER_REVIEW)) {
            throw new InvalidRequestException("Loan application cannot be approved");
        }
        LoanProduct loanProduct = loanProductRepository.findById(existingApplication.getLoanProductId())
                .orElseThrow(() -> new InvalidRequestException("Loan product not found"));

        BigDecimal approvedAmount = existingApplication.getApprovedAmount();

        existingApplication.setApprovedAmount(existingApplication.getRequestedAmount());
        existingApplication.setTenureMonths(loanProduct.getMaxTenureMonths());
        existingApplication.setInterestRatePercent(loanProduct.getInterestRatePercent());
        existingApplication.setInterestType(loanProduct.getInterestType());
        existingApplication.setTotalInterest(loanProduct.getInterestRatePercent().multiply(approvedAmount));
        existingApplication.setProcessingFee(loanProduct.getProcessingFeePercent().multiply(approvedAmount));
        existingApplication.setNetDisbursement(existingApplication.getProcessingFee().add(approvedAmount));
        existingApplication.setTotalRepayable(existingApplication.getNetDisbursement().add(existingApplication.getTotalInterest()));
        existingApplication.setMonthlyInstallment(existingApplication.getTotalRepayable().divide(existingApplication.getTenureMonths()));
        existingApplication.setLoanApplicationStatus(LoanApplicationStatus.APPROVED);
        loanApplicationRepository.save(existingApplication);
    }

    @Override
    public void rejectLoan(String loanApplicationId) {
        LoanApplication existingApplication = loanApplicationRepository.findById(loanApplicationId).orElseThrow(() ->
                new InvalidRequestException("Loan application with the id '" + loanApplicationId + "' does not exist"));
        LoanProduct loanProduct = loanProductRepository.findById(existingApplication.getLoanProductId())
                .orElseThrow(() -> new InvalidRequestException("Loan product not found"));
// savings account is not active... check for both parties
// purpose does not match with loan description
        if (loanProduct.getIsActive().equals(false)) {
        }
        if(loanProduct.getRequiresGuarantor().equals(false)){

        }
    }

    @Override
    public void disburseLoan(String loanApplicationId) {

    }

    @Override
    public void checkIfRepaid(String loanApplicationId) {

    }

    @Override
    public void addDefaulter(String loanApplicationId) {

    }

    @Override
    public void writeOff(String loanApplicationId) {

    }
}

