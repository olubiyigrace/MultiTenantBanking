package com.bank.services.impl;

import com.bank.auth.util.CurrentUserUtil;
import com.bank.config.InstitutionContext;
import com.bank.controllers.LoanOfficerController;
import com.bank.entities.*;
import com.bank.enums.LoanApplicationStatus;
import com.bank.enums.UserAccountType;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.InvalidRequestException;
import com.bank.mapper.LoanApplicationMapper;
import com.bank.repositories.LoanApplicationRepository;
import com.bank.repositories.LoanProductRepository;
import com.bank.repositories.MemberRepository;
import com.bank.requests.LoanApplicationRequest;
import com.bank.services.LoanApplicationService;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationMapper loanApplicationMapper;
    private final MemberRepository memberRepository;
    private final LoanProductRepository loanProductRepository;
    private final CurrentUserUtil currentUserUtil;

    @Override
    public void createApplication(LoanApplicationRequest loanApplicationRequest) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Creating loan application");

        User userId = currentUserUtil.getLoggedInUser();
        MemberProfile existingMember = memberRepository.findByUserId(userId.getId())
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
            log.debug("Cannot apply for a loan at the moment. You have an outstanding loan payment");
            throw new DuplicateResourceException("Cannot apply for a loan at the moment. You have an outstanding loan payment");
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
            throw new DuplicateResourceException("Cannot apply for a loan.");
        }
        LoanApplication loanApplication = loanApplicationMapper.toEntity(loanApplicationRequest);
        loanApplication.setInstitution(Institution.builder().id(institutionId).build());
        loanApplication.setMember(MemberProfile.builder().id(existingMember.getId()).build());
        loanApplication.setLoanApplicationStatus(LoanApplicationStatus.PENDING);
        loanApplicationRepository.save(loanApplication);
    }
}

