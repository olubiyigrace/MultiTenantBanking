package com.bank.loanguarantors;

import com.bank.others.exceptions.UnauthorizedException;
import com.bank.others.utils.CurrentUserUtil;
import com.bank.loanapplications.LoanApplicationStatus;
import com.bank.memberprofiles.ProfileStatus;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final GuarantorMapper guarantorMapper;
    private final SavingsRepository savingsRepository;


    @Override
    public void createGuarantor(GuarantorRequest guarantorRequest) {
        User loggedInUser = currentUserUtil.getLoggedInUser();

        boolean hasApplication = loanApplicationRepository.existsByMemberIdAndLoanApplicationStatus
                (loggedInUser.getMemberProfile().getId(), LoanApplicationStatus.PENDING);
        if (!hasApplication) {
            log.debug("No pending loan application");
            throw new InvalidRequestException("No pending loan application");
        }
        boolean requiresGuarantor = loanProductRepository.existsByInstitutionIdAndRequiresGuarantor(loggedInUser.getInstitutionId(), true);
        if (!requiresGuarantor) {
            log.debug("Loan product does not require a guarantor");
            throw new InvalidRequestException("Loan product does not require a guarantor");
        }

        MemberProfile existingMember = memberRepository.findById(guarantorRequest.getGuarantorMemberId())
                .orElseThrow(() -> new InvalidRequestException("Guarantor member id does not exist"));

        if (!existingMember.getProfileStatus().equals(ProfileStatus.ACTIVE)) {
            log.debug("Guarantor does not have an active profile status");
            throw new UnauthorizedException("Guarantor does not have an active profile status");
        }

        boolean hasSavings = savingsRepository.existsByMemberIdAndSavingsStatusAndSavingsAccountType(existingMember.getId(), SavingsStatus.ACTIVE, SavingsAccountType.FIXED);
        if (!hasSavings) {
            throw new InvalidRequestException("Guarantor has no active and/or fixed savings account to cover up for your loan");
        }
        Optional<LoanGuarantor> existingGuarantor = guarantorRepository.findByGuarantorMemberIdAndGuarantorStatus
                (guarantorRequest.getGuarantorMemberId(), GuarantorStatus.ACCEPTED);
        if (existingGuarantor.isPresent()) {
            throw new InvalidRequestException("Guarantor is already existing for another loan applicant");
        }
        LoanGuarantor loanGuarantor = guarantorMapper.toEntity(guarantorRequest);
        guarantorRepository.save(loanGuarantor);
    }


        //guarantor must have a fixed savings that is greater that applicant's total repayable and expiry is before applicant's maxTenure month
        //guarantor memberId must not have an existing ACTIVE LoanStatus
        // guarantor must have a null or fully repaid loanStatus
        // guarantor member status must be Active
        // guarantor must have a record of LoanStatus.FULLY_REPAID

    @Override
    public void verifyGuarantor(String guarantorMemberId){

       // if(guarantorMemberId.getIsVerified.equals(true)), set guarantor status to ACTIVE
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
