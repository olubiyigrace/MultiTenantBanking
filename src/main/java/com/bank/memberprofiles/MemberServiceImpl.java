package com.bank.memberprofiles;

import com.bank.institutions.InstitutionRepository;
import com.bank.others.exceptions.InvalidRequestException;
import com.bank.others.exceptions.UnauthorizedException;
import com.bank.others.utils.CurrentUserUtil;
import com.bank.savingsaccount.SavingsStatus;
import com.bank.users.UserRepository;
import com.bank.institutions.Institution;
import com.bank.savingsaccount.SavingsAccount;
import com.bank.users.User;
import com.bank.savingsaccount.SavingsAccountType;
import com.bank.others.exceptions.DuplicateResourceException;
import com.bank.savingsaccount.SavingsRepository;
import com.bank.others.utils.PageResponse;
import com.bank.others.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final UserRepository userRepository;
    private final SavingsRepository savingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CurrentUserUtil currentUserUtil;
    private final InstitutionRepository institutionRepository;


    @Override
    public void createMember(MemberRequest memberRequest) throws MessagingException {
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
                member.getUser().setEmailVerificationToken("used");
                member.getUser().setEmailVerificationTokenExpiry(null);

            SavingsAccount savingsAccount = SavingsAccount.builder()
                    .accountNumber(generateAccountNumber(loggedInUser.getInstitution()))
                    .balance(memberRequest.getSavingsAccountRequest().getBalance())
                    .targetAmount(memberRequest.getSavingsAccountRequest().getTargetAmount())
                    .maturityDate(memberRequest.getSavingsAccountRequest().getMaturityDate())
                    .interestRatePercent(BigDecimal.valueOf(0.00034369))
                    .minimumBalance(BigDecimal.valueOf(0.0))
                    .savingsAccountType(SavingsAccountType.REGULAR)
                    .member(member)
                    .institution(institution)
                    .build();

            member.setProfileStatus(ProfileStatus.ACTIVE);
            savingsAccount.setSavingsStatus(SavingsStatus.ACTIVE);
            userRepository.save(member.getUser());
            memberRepository.save(member);
            savingsRepository.save(savingsAccount);

                emailService.sendAccountNumberEmail(
                        member.getUser().getEmail(),
                        savingsAccount.getAccountNumber(),
                        member.getInstitution().getInstitutionName()
                );
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
                    .interestRatePercent(BigDecimal.valueOf(0.0005))
                    .minimumBalance(BigDecimal.valueOf(0.0))
                    .savingsAccountType(SavingsAccountType.REGULAR)
                    .member(member)
                    .institution(institution)
                    .build();

            String emailVerificationToken = UUID.randomUUID().toString();
            member.getUser().setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
            member.getUser().setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(member.getUser());
            memberRepository.save(member);
            savingsRepository.save(savingsAccount);

            Map<String, Object> model = new HashMap<>();
            model.put("name", memberRequest.getRegisterUserRequest().getName());
            model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/verify?"
                    + "token=" + emailVerificationToken);

            emailService.sendVerificationEmail(
                    memberRequest.getRegisterUserRequest().getEmail(),
                    "Verify your account",
                    "userverification",
                    model
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

