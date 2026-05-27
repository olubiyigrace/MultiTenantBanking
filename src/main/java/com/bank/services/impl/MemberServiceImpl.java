package com.bank.services.impl;

import com.bank.auth.repository.UserRepository;
import com.bank.config.InstitutionContext;
import com.bank.entities.Institution;
import com.bank.entities.MemberProfile;
import com.bank.entities.SavingsAccount;
import com.bank.entities.User;
import com.bank.enums.ProfileStatus;
import com.bank.enums.SavingsAccountType;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.mapper.MemberMapper;
import com.bank.repositories.MemberRepository;
import com.bank.repositories.SavingsRepository;
import com.bank.requests.MemberRequest;
import com.bank.responses.MemberResponse;
import com.bank.responses.PageResponse;
import com.bank.services.EmailService;
import com.bank.services.MemberService;
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
public class MemberServiceImpl implements MemberService {
        private final MemberRepository memberRepository;
        private final MemberMapper memberMapper;
        private final UserRepository userRepository;
        private final SavingsRepository savingsRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;


        @Transactional
        public void createMember(MemberRequest memberRequest) throws MessagingException {
            final String institutionId = InstitutionContext.getCurrentInstitution();

            Optional<MemberProfile> existingMember = memberRepository.findByBvn(memberRequest.getBvn());
            if (existingMember.isPresent()) {
                log.debug("Member already exists");
                throw new DuplicateResourceException("Member already exists");
            }

            Optional<User> existingUser = userRepository.findByEmail(memberRequest.getRegisterUserRequest().getEmail());
            if (existingUser.isPresent()) {
                log.debug("User already exists");
                throw new DuplicateResourceException("User already exists");
            }

            MemberProfile member = memberMapper.toEntity(memberRequest);
            member.setMemberNumber(generateMemberNumber());

            Institution institution = Institution.builder().id(institutionId).build();
            member.setInstitution(institution);
            member.getUser().setInstitution(institution);

            SavingsAccount savingsAccount = SavingsAccount.builder()
                    .accountNumber(generateAccountNumber())
                    .balance(memberRequest.getSavingsAccountRequest().getBalance())
                    .targetAmount(memberRequest.getSavingsAccountRequest().getTargetAmount())
                    .maturityDate(memberRequest.getSavingsAccountRequest().getMaturityDate())
                    .interestRatePercent(BigDecimal.valueOf(0.01))
                    .minimumBalance(BigDecimal.valueOf(0.0))
                    .savingsAccountType(SavingsAccountType.REGULAR)
                    .member(member)
                    .institution(institution)
                    .build();

            String emailVerificationToken = UUID.randomUUID().toString();
            institution.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
            institution.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));

            userRepository.save(member.getUser());
            memberRepository.save(member);
            savingsRepository.save(savingsAccount);

            Map<String, Object> model = new HashMap<>();
            model.put("name", memberRequest.getRegisterUserRequest().getName());
            model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/verify?token=" + emailVerificationToken);

            emailService.sendVerificationEmail(
                    memberRequest.getRegisterUserRequest().getEmail(),
                    "Verify your account",
                    "userverification",
                    model
            );
        }

    @Override
    public PageResponse<MemberResponse> getAllMembers(ProfileStatus profileStatus, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<MemberProfile> memberProfiles;
        if (profileStatus != null) {
            memberProfiles = memberRepository.findByProfileStatus(profileStatus, pageRequest);
        } else {
            memberProfiles = memberRepository.findAll(pageRequest);
        }
        Page<MemberResponse> memberResponses = memberProfiles.map(memberMapper::toResponse);
        return PageResponse.of(memberResponses);
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with the username '" + username + "' not found"));
    }

    private String generateMemberNumber() {
        MemberProfile lastMember = memberRepository.findTopByOrderByCreatedAtDesc().orElse(null);
        if (lastMember == null) {
            return "M0000000000001";
        }
        String lastMemberNumber = lastMember.getMemberNumber();
        long numericPart = Long.parseLong(lastMemberNumber.substring(1));
        long nextNumber = numericPart + 1;
        return String.format("M%013d", nextNumber);
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

