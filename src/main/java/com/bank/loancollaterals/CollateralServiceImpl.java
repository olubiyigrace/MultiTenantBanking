package com.bank.loancollaterals;

import com.bank.memberprofiles.MemberProfile;
import com.bank.memberprofiles.MemberRepository;
import com.bank.others.utils.CurrentUserUtil;
import com.bank.loanapplications.LoanApplication;
import com.bank.loanproducts.LoanProductRepository;
import com.bank.users.User;
import com.bank.loanapplications.LoanApplicationStatus;
import com.bank.others.exceptions.InvalidRequestException;
import com.bank.loanapplications.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CollateralServiceImpl implements CollateralService {
    private final CollateralRepository collateralRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final CurrentUserUtil currentUserUtil;
    private final CollateralMapper collateralMapper;
    private final LoanProductRepository loanProductRepository;
    private final MemberRepository memberRepository;


    @Override
    public void createCollateral(LoanCollateralRequest loanCollateralRequest) {
        User loggedInUser = currentUserUtil.getLoggedInUser();
        MemberProfile memberProfile = memberRepository.findByUserIdAndInstitutionId(loggedInUser.getId(),
                loggedInUser.getInstitutionId()).orElseThrow(() -> new InvalidRequestException("Member profile not found"));

        LoanApplication application = loanApplicationRepository.findByMemberIdAndLoanApplicationStatus(
                        memberProfile.getId(), LoanApplicationStatus.PENDING).orElseThrow(() ->
                        new InvalidRequestException("No pending loan application found."));

        boolean requiresCollateral = loanProductRepository.existsByInstitutionIdAndRequiresCollateral(loggedInUser.getInstitutionId(), true);
        if (!requiresCollateral) {
            log.debug("Loan product does not require a collateral");
            throw new InvalidRequestException("Loan product does not require a collateral");
        }

       if(loanCollateralRequest.getEstimatedValue().compareTo(application.getRequestedAmount()) < 0){
           throw new InvalidRequestException("Collateral estimated value must be greater than the requested amount");
       }
        LoanCollateral loanCollateral = collateralMapper.toEntity(loanCollateralRequest);
        loanCollateral.setLoanApplication(LoanApplication.builder().id(application.getId()).build());
            collateralRepository.save(loanCollateral);
        }

    public void deleteCollateral(String loanCollateralId){
        Optional<LoanCollateral> loanCollateral = collateralRepository.findById(loanCollateralId);
        if(loanCollateral.isEmpty()){
            throw new InvalidRequestException("Loan collateral with the id does not exist");
        }
        LoanCollateral existingCollateral = loanCollateral.get();
        collateralRepository.delete(existingCollateral);
    }
}
