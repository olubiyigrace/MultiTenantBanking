package com.bank.services.servicesImpl;

import com.bank.dtos.requestDtos.MemberRequest;
import com.bank.dtos.requestDtos.OtpRequest;
import com.bank.dtos.responseDtos.MemberResponse;
import com.bank.entities.MemberProfile;
import com.bank.enums.OtpPurpose;
import com.bank.enums.ProfileStatus;
import com.bank.orders.OrderProducer;
import com.bank.orders.ProducerMessage;
import com.bank.repositories.InstitutionRepository;
import com.bank.mappers.MemberMapper;
import com.bank.exceptions.InvalidRequestException;
import com.bank.exceptions.UnauthorizedException;
import com.bank.services.MemberService;
import com.bank.services.OtpService;
import com.bank.utils.CurrentUserUtil;
import com.bank.repositories.MemberRepository;
import com.bank.enums.SavingsStatus;
import com.bank.repositories.UserRepository;
import com.bank.entities.Institution;
import com.bank.entities.SavingsAccount;
import com.bank.entities.User;
import com.bank.enums.SavingsAccountType;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.repositories.SavingsRepository;
import com.bank.utils.PageResponse;
import com.bank.services.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final UserRepository userRepository;
    private final SavingsRepository savingsRepository;
    private final EmailService emailService;
    private final CurrentUserUtil currentUserUtil;
    private final InstitutionRepository institutionRepository;
    private final OtpService otpService;
    private final OrderProducer orderProducer;


    @Override
    public void createMember(MemberRequest memberRequest){
        User loggedInUser = currentUserUtil.getLoggedInUser();

        Optional<MemberProfile> existingMember = memberRepository.findByBvnAndInstitutionId(memberRequest.getBvn(), loggedInUser.getInstitutionId());
        if (existingMember.isPresent()) {
            log.debug("Member already exists");
            throw new DuplicateResourceException("Member already exists");
        }

        Optional<User> existingUser = userRepository.findByEmail(memberRequest.getRegisterUserRequest().getEmail());
        MemberProfile member = memberMapper.toEntity(memberRequest);
        member.setMemberNumber(generateMemberNumber(loggedInUser.getInstitution()));
        Institution institution = Institution.builder().id(loggedInUser.getInstitutionId()).build();
        member.setInstitution(Institution.builder().id(loggedInUser.getInstitutionId()).build());

        if (existingUser.isPresent() && existingUser.get().getIsVerified().equals(false)){
            throw new InvalidRequestException("Email has been previously registered with another institution but not verified, register as a new member with a different email");
        }
        else if (existingUser.isPresent() && existingUser.get().getIsVerified().equals(true)) {
            member.setUser(existingUser.get());
            if (existingUser.get().getMemberProfiles().get(0).getBvn().equals(memberRequest.getBvn()) &&
                    existingUser.get().getMemberProfiles().get(0).getDateOfBirth().equals(memberRequest.getDateOfBirth())) {
                member.getUser().setInstitution(Institution.builder().id(loggedInUser.getInstitutionId()).build());
                //how does a new institution have the record of their new user who has been an existing member to another institution?

            SavingsAccount savingsAccount = SavingsAccount.builder()
                    .accountNumber(generateAccountNumber(loggedInUser.getInstitution()))
                    .balance(memberRequest.getSavingsAccountRequest().getBalance())
                    .targetAmount(memberRequest.getSavingsAccountRequest().getTargetAmount())
                    .maturityDate(memberRequest.getSavingsAccountRequest().getMaturityDate())
                    .interestRatePercent(BigDecimal.valueOf(0.00021918))
                    .minimumBalance(BigDecimal.valueOf(0.0))
                    .savingsAccountType(SavingsAccountType.REGULAR)
                    .member(member)
                    .institution(institution)
                    .build();

            member.setProfileStatus(ProfileStatus.ACTIVE);
            savingsAccount.setSavingsStatus(SavingsStatus.ACTIVE);
            userRepository.save(member.getUser());
            memberRepository.save(member);

                if (!Boolean.TRUE.equals(
                        savingsAccount.getAccountNumberEmailSent())) {

                    emailService.sendAccountNumberEmail(
                            member.getUser().getEmail(),
                            savingsAccount.getAccountNumber(),
                            loggedInUser.getInstitution().getInstitutionName()
                    );
                    savingsAccount.setAccountNumberEmailSent(true);
                    savingsRepository.save(savingsAccount);
                }
            } else {
                throw new InvalidRequestException("Either the bvn or the date of birth is incorrect or both are incorrect");
            }
        }  else {
            member.getUser().setInstitution(Institution.builder().id(loggedInUser.getInstitutionId()).build());

            SavingsAccount savingsAccount = SavingsAccount.builder()
                    .accountNumber(generateAccountNumber(loggedInUser.getInstitution()))
                    .balance(memberRequest.getSavingsAccountRequest().getBalance())
                    .targetAmount(memberRequest.getSavingsAccountRequest().getTargetAmount())
                    .maturityDate(memberRequest.getSavingsAccountRequest().getMaturityDate())
                    .interestRatePercent(BigDecimal.valueOf(0.00021918))
                    .minimumBalance(BigDecimal.valueOf(0.0))
                    .savingsAccountType(SavingsAccountType.REGULAR)
                    .member(member)
                    .institution(institution)
                    .build();
            userRepository.save(member.getUser());
            memberRepository.save(member);
            savingsRepository.save(savingsAccount);

            String otp = otpService.createOtp(
                    OtpRequest.builder()
                            .email(member.getUser().getEmail())
                            .purpose(OtpPurpose.VERIFY_ACCOUNT)
                            .build()
            );
            orderProducer.sendMessage(
                    ProducerMessage.builder()
                            .email(member.getUser().getEmail())
                            .otp(otp)
                            .purpose(OtpPurpose.VERIFY_ACCOUNT)
                            .build()
            );
        }
    }

    private String generateMemberNumber(Institution institution) {
        Long sequence = institutionRepository.getNextMemberSequence(institution.getId());
        return "M" + String.format("%09d", sequence);
    }

    private String generateAccountNumber(Institution institution) {
        long count = savingsRepository.countByInstitutionId(institution.getId());
        long nextNumber = count + 1;
        return institution.getInstitutionCode() + String.format("%06d", nextNumber);
    }

    @Override
    public PageResponse<MemberResponse> getAllMembers(ProfileStatus profileStatus, int page, int size) {
        User loggedInUser = currentUserUtil.getLoggedInUser();

        if (loggedInUser.getInstitution() == null) {
            throw new UnauthorizedException("User is not linked to any institution");
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<MemberProfile> memberProfiles = memberRepository.findByInstitutionAndProfileStatus(loggedInUser.getInstitution(), profileStatus, pageRequest);
        if (profileStatus == null) {
            throw new InvalidRequestException("Profile status is required");
        }
        return PageResponse.of(memberProfiles.map(memberMapper::toResponse));
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with the username '" + username + "' not found"));
    }
}

