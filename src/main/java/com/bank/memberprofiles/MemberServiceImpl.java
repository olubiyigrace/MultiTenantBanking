package com.bank.memberprofiles;

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
import java.security.SecureRandom;
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
        member.setMemberNumber(generateMemberNumber());
        Institution institution = Institution.builder().id(loggedInUser.getInstitutionId()).build();
        member.setInstitution(Institution.builder().id(loggedInUser.getInstitutionId()).build());

        if (existingUser.isPresent()) {
            member.setUser(existingUser.get());
            if (existingUser.get().getEmail().equals(memberRequest.getRegisterUserRequest().getEmail())) {
                member.getUser().setInstitution(Institution.builder().id(loggedInUser.getInstitutionId()).build());
                member.getUser().setEmailVerificationToken("used");
                member.getUser().setEmailVerificationTokenExpiry(null);

            SavingsAccount savingsAccount = SavingsAccount.builder()
                    .accountNumber(generateAccountNumber())
                    .balance(memberRequest.getSavingsAccountRequest().getBalance())
                    .targetAmount(memberRequest.getSavingsAccountRequest().getTargetAmount())
                    .maturityDate(memberRequest.getSavingsAccountRequest().getMaturityDate())
                    .interestRatePercent(BigDecimal.valueOf(0.0005))
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
            } else {
                throw new InvalidRequestException("Email does not match with the previous");
            }
        } else {
            member.getUser().setInstitution(Institution.builder().id(loggedInUser.getInstitutionId()).build());

            SavingsAccount savingsAccount = SavingsAccount.builder()
                    .accountNumber(generateAccountNumber())
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

    private String generateMemberNumber() {
        MemberProfile lastMember = memberRepository.findTopByOrderByCreatedAtDesc().orElse(null);
        if (lastMember == null) {
            return "M001";
        }
        String lastMemberNumber = lastMember.getMemberNumber();
        long numericPart = Long.parseLong(lastMemberNumber.substring(1));
        long nextNumber = numericPart + 1;
        return String.format("M%03d", nextNumber);
    }

    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            accountNumber.append(random.nextInt(10));
        }
        return accountNumber.toString();
    }
}

