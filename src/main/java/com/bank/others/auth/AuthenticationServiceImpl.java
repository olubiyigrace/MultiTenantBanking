package com.bank.others.auth;

import com.bank.institutions.*;
import com.bank.memberprofiles.MemberProfile;
import com.bank.memberprofiles.MemberRepository;
import com.bank.memberprofiles.ProfileStatus;
import com.bank.others.login.*;
import com.bank.others.password.ChangePasswordRequest;
import com.bank.others.password.ForgotPasswordRequest;
import com.bank.others.password.ResetPasswordRequest;
import com.bank.others.usersession.UserSession;
import com.bank.others.usersession.UserSessionRepository;
import com.bank.others.logout.LogoutToken;
import com.bank.others.logout.LogoutTokenRepository;
import com.bank.others.utils.CurrentUserUtil;
import com.bank.others.services.EmailService;
import com.bank.savingsaccount.SavingsAccount;
import com.bank.savingsaccount.SavingsRepository;
import com.bank.savingsaccount.SavingsStatus;
import com.bank.users.*;
import com.bank.others.exceptions.DuplicateResourceException;
import com.bank.others.exceptions.InvalidRequestException;
import com.bank.others.exceptions.UnauthorizedException;
import com.bank.others.securities.JwtService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserUtil currentUserUtil;
    private final LogoutTokenRepository logoutTokenRepository;
    private final UserSessionRepository userSessionRepository;
    private final MemberRepository memberRepository;
    private final SavingsRepository savingsRepository;


    @Override
    public void registerInstitution(final RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException {
        if (institutionRepository.existsByInstitutionRcNumber(registerInstitutionRequest.getInstitutionRcNumber())) {
            log.debug("Institution with the RC Number '{}' has been registered.", registerInstitutionRequest.getInstitutionRcNumber());
            throw new DuplicateResourceException("Institution with the RC Number '" + registerInstitutionRequest.getInstitutionRcNumber() + "' already exists..");
        }
        if (institutionRepository.existsByInstitutionEmail(registerInstitutionRequest.getInstitutionEmail())) {
            log.debug("Institution with the email '{}' has been registered.", registerInstitutionRequest.getInstitutionEmail());
            throw new DuplicateResourceException("Institution with the email '" + registerInstitutionRequest.getInstitutionEmail() + "' already exists.");
        }
        final Institution institution = institutionMapper.toEntity(registerInstitutionRequest);
        institution.setInstitutionStatus(InstitutionStatus.PENDING);
        String emailVerificationToken = UUID.randomUUID().toString();
        institution.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
        institution.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
        institutionRepository.save(institution);

        Map<String, Object> model = new HashMap<>();
        model.put("institutionName", registerInstitutionRequest.getInstitutionName());
        model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/verify?" + emailVerificationToken);

        emailService.sendVerificationEmail(
                registerInstitutionRequest.getInstitutionEmail(),
                "Verify your account",
                "institutionverification",
                model
        );
    }

    @Override
    public void verifyEmail(final String verificationTokenFromRequest, final String email) {
        Institution institution = institutionRepository.findByInstitutionEmail(email)
                .orElseThrow(() -> new InvalidRequestException("Institution with the email '" + email + "' does not exist. Visit the website to register"));
        if (!passwordEncoder.matches(verificationTokenFromRequest, institution.getEmailVerificationToken())) {
            log.debug("Invalid token");
            throw new InvalidRequestException("Invalid token");
        }
        if (passwordEncoder.matches(verificationTokenFromRequest, institution.getEmailVerificationToken()) && institution.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            log.debug("Token has expired");
            throw new InvalidRequestException("Token has expired");
        }
        institution.setEmailVerifiedAt(LocalDateTime.now());
        institution.setIsVerified(true);
        institution.setEmailVerificationToken("used");
        institution.setEmailVerificationTokenExpiry(null);
        institutionRepository.save(institution);
    }

    @Override
    public void reverifyInstitutionEmail(final String email) {
        Institution institution = institutionRepository.findByInstitutionEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Institution with the email '" + email + "'  does not exist. Visit the website to create an account."));

        if (Boolean.TRUE.equals(institution.getIsVerified())) {
            log.debug("Institution already verified");
            throw new DuplicateResourceException("Institution already verified");
        }
        String emailVerificationToken = UUID.randomUUID().toString();
        institution.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
        institution.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
        institutionRepository.save(institution);

        Map<String, Object> model = new HashMap<>();
        model.put("institutionName", institution.getInstitutionName());
        model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/resend-verification?" + emailVerificationToken);
        try {
            emailService.sendVerificationEmail(
                    institution.getInstitutionEmail(),
                    "Verify your account",
                    "institutionverification",
                    model
            );
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createUser(RegisterUserRequest registerUserRequest) throws MessagingException {
        User loggedInUser = currentUserUtil.getLoggedInUser();

        log.info("Creating user for institution: {}", loggedInUser.getInstitutionId());
        if (userRepository.existsByEmail(registerUserRequest.getEmail())) {
            log.debug("User with the email '{}' already exists.", registerUserRequest.getEmail());
            throw new DuplicateResourceException("User with the email '" + registerUserRequest.getEmail() + "' already exists.");
        }
        if (userRepository.existsByUsername(registerUserRequest.getEmail())) {
            log.debug("User with the username '{}' already exists.", registerUserRequest.getEmail());
            throw new DuplicateResourceException("User with the username '" + registerUserRequest.getEmail() + "' already exists.");
        }
        if (registerUserRequest.getUserAccountType() == UserAccountType.SUPER_ADMIN || registerUserRequest.getUserAccountType() == UserAccountType.INSTITUTION_ADMIN
        ) {
            throw new InvalidRequestException("SUPER_ADMIN and INSTITUTION_ADMIN cannot be selected as an account type");
        }

        final User user = userMapper.toEntity(registerUserRequest);
        user.setInstitution(Institution.builder().id(loggedInUser.getInstitutionId()).build());
        String emailVerificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        log.info("User created successfully!");

        Map<String, Object> model = new HashMap<>();
        model.put("name", registerUserRequest.getName());
        model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/verify?token=" + emailVerificationToken);

        emailService.sendVerificationEmail(
                registerUserRequest.getEmail(),
                "Verify your account",
                "userverification",
                model
        );
    }

    @Override
    public void verifyUser(final String verificationTokenFromRequest, final String email) {
        log.info("Verifying user");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidRequestException("User with the email '" + email + "' does not exist. Visit the website to register"));
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new InvalidRequestException("User has already been verified");
        }
        if (!passwordEncoder.matches(verificationTokenFromRequest, user.getEmailVerificationToken())) {
            log.debug("Invalid token");
            throw new InvalidRequestException("Invalid token");
        }
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            log.debug("Token has expired");
            throw new InvalidRequestException("Token has expired");
        }
        user.setIsVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setEmailVerificationToken("used");
        user.setEmailVerificationTokenExpiry(null);

        List<MemberProfile> profiles = memberRepository.findByUserId(user.getId());
        for (MemberProfile profile : profiles) {
            profile.setProfileStatus(ProfileStatus.ACTIVE);

            savingsRepository.findByMember(profile)
                    .ifPresent(account -> {
                        account.setSavingsStatus(SavingsStatus.ACTIVE);
                        savingsRepository.save(account);
                    });
        }
        memberRepository.saveAll(profiles);
        userRepository.save(user);

        for (MemberProfile member : profiles) {
                SavingsAccount savingsAccount = savingsRepository.findByMemberId(member.getId())
                        .orElseThrow(() -> new InvalidRequestException("Savings account not found for member"));

                if (!Boolean.TRUE.equals(
                        savingsAccount.getAccountNumberEmailSent())) {

                emailService.sendAccountNumberEmail(
                        user.getEmail(),
                        savingsAccount.getAccountNumber(),
                        member.getInstitution().getInstitutionName()
                );
                savingsAccount.setAccountNumberEmailSent(true);
                savingsRepository.save(savingsAccount);

                log.info("User verified successfully");
            }
        }
    }

    @Override
    public void resendUserVerificationToken(final String email) {
        log.info("Resending user email verification token");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("user with the email '" + email + "'  does not exist. Visit the website to create an account."));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            log.debug("User already verified");
            throw new DuplicateResourceException("User already verified");
        }
        String emailVerificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        log.info("User email verification token resent");

        Map<String, Object> model = new HashMap<>();
        model.put("name", user.getName());
        model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/resend-verification?" + emailVerificationToken);
        try {
            emailService.sendVerificationEmail(
                    user.getEmail(),
                    "Verify your account",
                    "userverification",
                    model
            );
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SelectInstitutionResponse preLogin(SelectInstitutionRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid credentials");
        }
        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new InvalidRequestException("User not verified");
        }
        if (user.getUserAccountType() == UserAccountType.SUPER_ADMIN) {
            String loginToken = jwtService.generateLoginToken(user.getId());
            return SelectInstitutionResponse.builder()
                    .loginType("SINGLE")
                    .loginToken(loginToken)
                    .institutions(List.of())
                    .build();
        }

        if (user.getUserAccountType() != UserAccountType.MEMBER) {
            Institution institution = institutionRepository.findById(user.getInstitutionId())
                    .orElseThrow(() -> new InvalidRequestException("Institution not found"));
            String loginToken = jwtService.generateLoginToken(user.getId());
            if (institution.getInstitutionStatus() != InstitutionStatus.ACTIVE){
                throw new InvalidRequestException("Your institution is not active");
            }
            return SelectInstitutionResponse.builder()
                    .loginType("SINGLE")
                    .loginToken(loginToken)
                    .institutions(List.of(
                            new UserInstitutionsResponse(
                                    institution.getId(),
                                    institution.getInstitutionName(),
                                    institution.getInstitutionType(),
                                    institution.getInstitutionStatus())
                    )).build();
        }
        List<MemberProfile> profiles = memberRepository.findByUserId(user.getId());
        List<UserInstitutionsResponse> institutions = profiles.stream()
                        .map(p -> new UserInstitutionsResponse(
                                p.getInstitution().getId(),
                                p.getInstitution().getInstitutionName(),
                                p.getInstitution().getInstitutionType(),
                                p.getInstitution().getInstitutionStatus()
                        ))
                        .toList();

        String loginToken = jwtService.generateLoginToken(user.getId());
        return SelectInstitutionResponse.builder()
                .loginType("MULTI")
                .loginToken(loginToken)
                .institutions(institutions)
                .build();
    }

    public LoginResponse login(LoginRequest request) throws MessagingException {
        jwtService.validateLoginToken(request.getLoginToken());
        String userId = jwtService.getUserIdFromLoginToken(request.getLoginToken());
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Institution institution;
        if (user.getUserAccountType().equals(UserAccountType.SUPER_ADMIN)) {
            String token = UUID.randomUUID().toString();
            user.setResetPasswordToken(passwordEncoder.encode(token));
            user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);

            String accessToken = jwtService.generateAccessToken(
                    "not_required",
                    user.getId(),
                    user.getUserAccountType().name());

            String refreshToken = jwtService.generateRefreshToken(
                   "not_required",
                    user.getId(),
                    user.getUserAccountType().name());

            Map<String, Object> model = new HashMap<>();
            model.put("name", user.getName());
            model.put("resetUrl", "https://multitenantbank.com/api/v1/auth/reset-password?token=" + token);
            model.put("revokeUrl", "https://multitenantbanking.com/api/v1/auth/revoke-session?token=" + accessToken);

            emailService.sendVerificationEmail(
                    user.getUsername(),
                    "New Login Alert!",
                    "login",
                    model);

            UserSession session = UserSession.builder()
                    .accessToken(accessToken)
                    .revoked(false)
                    .expiryDate(jwtService.extractExpiration(accessToken).toInstant())
                    .user(user)
                    .build();
            userSessionRepository.save(session);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .build();
        }
        if (user.getUserAccountType() != UserAccountType.MEMBER) {
            if (!user.getInstitutionId().equals(request.getInstitutionId())) {
                throw new InvalidRequestException("User not assigned to this institution");
            }
            institution = institutionRepository.findById(user.getInstitutionId())
                    .orElseThrow(() -> new InvalidRequestException("Institution not found"));
        } else {
            MemberProfile profile = memberRepository.findByUserIdAndInstitutionId(userId, request.getInstitutionId())
                    .orElseThrow(() -> new InvalidRequestException("Not a member of this institution"));
            institution = profile.getInstitution();
        }
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(passwordEncoder.encode(token));
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(
                institution.getId(),
                user.getId(),
                user.getUserAccountType().name());

        String refreshToken = jwtService.generateRefreshToken(
                institution.getId(),
                user.getId(),
                user.getUserAccountType().name());

        Map<String, Object> model = new HashMap<>();
        model.put("name", user.getName());
        model.put("resetUrl", "https://multitenantbank.com/api/v1/auth/reset-password?token=" + token);
        model.put("revokeUrl", "https://multitenantbanking.com/api/v1/auth/revoke-session?token=" + accessToken);

        emailService.sendVerificationEmail(
                user.getUsername(),
                "New Login Alert!",
                "login",
                model);

        UserSession session = UserSession.builder()
                .accessToken(accessToken)
                .revoked(false)
                .expiryDate(jwtService.extractExpiration(accessToken).toInstant())
                .user(user)
                .build();
        userSessionRepository.save(session);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User loggedInUser = currentUserUtil.getLoggedInUser();

        boolean matches = passwordEncoder.matches(request.getOldPassword(), loggedInUser.getPassword());
        if (!matches) throw new InvalidRequestException("Old password is incorrect");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), loggedInUser.getPassword())) {
            throw new InvalidRequestException("Cannot reuse old password");
        }
        loggedInUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(loggedInUser);
        log.info("password changed successfully");
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) throws MessagingException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(passwordEncoder.encode(token));
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        Map<String, Object> model = new HashMap<>();
        model.put("name", user.getName());
        model.put("resetUrl", "https://multitenantbanking.com/api/v1/auth/reset-password?token=" + token);

        emailService.sendVerificationEmail(
                user.getEmail(),
                "Reset Password",
                "forgotpassword",
                model
        );
        log.info("Reset link sent");
    }

    @Override
    public void resetPasswordWithToken(String token, ResetPasswordRequest request) {
        User user = userRepository.findAll().stream()
                .filter(u -> u.getResetPasswordToken() != null)
                .filter(u -> passwordEncoder.matches(token, u.getResetPasswordToken()))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("Invalid token"));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("Token expired");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken("used");
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        log.info("password reset with token successfully");
    }

    @Override
    public LoginResponse refreshToken(final RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();
        if (!jwtService.isRefreshToken(refreshToken)) {
            log.debug("Invalid refresh token");
            throw new InvalidRequestException("Invalid refresh token");
        }
        jwtService.validateToken(refreshToken);

        String userId = jwtService.getUserIdFromRefreshToken(refreshToken);
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        final String newAccessToken = jwtService.generateAccessToken(
                user.getInstitutionId(),
                user.getId(),
                user.getUserAccountType().name());
        final String newRefreshToken = jwtService.generateRefreshToken(
                user.getInstitutionId(),
                user.getId(),
                user.getUserAccountType().name());
        return new LoginResponse(newAccessToken, newRefreshToken, "Bearer");
    }

    @Override
    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Invalid authorization header");
            throw new InvalidRequestException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        UserSession userSession = userSessionRepository.findByAccessToken(token)
                .orElseThrow(() -> new InvalidRequestException("Session already ended"));
        Instant expiryDate = jwtService.extractExpiration(token).toInstant();

        LogoutToken logoutToken = LogoutToken.builder()
                .token(token)
                .expiryDate(expiryDate)
                .userSession(userSession)
                .build();
        logoutTokenRepository.save(logoutToken);

        userSession.setRevoked(true);
        userSession.setAccessToken("used");
        userSessionRepository.save(userSession);
    }


    @Override
    public void revokeSession(String accessToken) {
        UserSession session = userSessionRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new DuplicateResourceException("Session already ended"));
        if (session.isRevoked()){
            throw new UnauthorizedException("Session has been revoked");
        }

        Instant expiryDate = jwtService.extractExpiration(accessToken).toInstant();
        LogoutToken logoutToken = LogoutToken.builder()
                .token(accessToken)
                .expiryDate(expiryDate)
                .userSession(session)
                .build();

        session.setRevoked(true);
        session.setAccessToken("used");
        logoutTokenRepository.save(logoutToken);
        userSessionRepository.save(session);
    }
}