package com.bank.services.impl;

import com.bank.auth.util.CurrentUserUtil;
import com.bank.entities.*;
import com.bank.enums.GuarantorStatus;
import com.bank.enums.LoanApplicationStatus;
import com.bank.exceptions.InvalidRequestException;
import com.bank.repositories.GuarantorRepository;
import com.bank.repositories.LoanApplicationRepository;
import com.bank.repositories.LoanProductRepository;
import com.bank.repositories.MemberRepository;
import com.bank.requests.GuarantorRequest;
import com.bank.responses.GuarantorResponse;
import com.bank.responses.PageResponse;
import com.bank.services.GuarantorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuarantorServiceImpl implements GuarantorService {
    private final GuarantorRepository guarantorRepository;
    private final MemberRepository memberRepository;
    private final CurrentUserUtil currentUserUtil;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanProductRepository loanProductRepository;


    @Override
    public void createGuarantor(GuarantorRequest guarantorRequest) {
        User loggedInUser = currentUserUtil.getLoggedInUser();

        boolean hasApplication = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus
                (loggedInUser.getMemberProfile().getId(), LoanApplicationStatus.PENDING);
        if (!hasApplication) {
            log.debug("You are yet to apply for a loan");
            throw new InvalidRequestException("You are yet to apply for a loan");
        }

        boolean requiresGuarantor = loanProductRepository.existsByInstitutionIdAndRequiresGuarantorIs
                (loggedInUser.getInstitutionId(), true);
        if (!requiresGuarantor) {
            log.debug("Loan product does not require a guarantor");
            throw new InvalidRequestException("Loan product does not require a guarantor");
        }

        MemberProfile existingMember = memberRepository.findById(guarantorRequest.getGuarantorMemberId())
                .orElseThrow(() -> new InvalidRequestException("Guarantor member id does not exist"));

        Optional<LoanGuarantor> existingGuarantor = guarantorRepository.findByGuarantorMemberIdAndGuarantorStatus
                (guarantorRequest.getGuarantorMemberId(), GuarantorStatus.ACCEPTED);
        if(existingGuarantor.isPresent()){
            throw new InvalidRequestException("Guarantor already exists");

    }
        LoanGuarantor loanGuarantor = LoanGuarantor.builder()
                .guarantorMemberId(guarantorRequest.getGuarantorMemberId())
                .guarantorStatus(GuarantorStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
//        loanGuarantor.setLoanApplication(existin;
        guarantorRepository.save(loanGuarantor);
    }

    @Override
    public PageResponse<GuarantorResponse> getAllGuarantors(int page, int size) {
        return null;
    }

    @Override
    public void updateGuarantor(String id, GuarantorRequest guarantorRequest) {
    }

    @Override
    public void deleteGuarantor(String id) {

    }
}
