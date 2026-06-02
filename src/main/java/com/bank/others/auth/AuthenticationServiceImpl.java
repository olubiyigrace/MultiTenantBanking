package com.bank.others.auth;

import com.bank.others.password.ChangePasswordRequest;
import com.bank.others.password.ForgotPasswordRequest;
import com.bank.others.password.ResetPasswordRequest;
import com.bank.others.usersession.UserSession;
import com.bank.others.usersession.UserSessionRepository;
import com.bank.others.login.LoginRequest;
import com.bank.others.login.LoginResponse;
import com.bank.others.logout.LogoutToken;
import com.bank.others.logout.LogoutTokenRepository;
import com.bank.others.utils.CurrentUserUtil;
import com.bank.others.services.EmailService;
import com.bank.others.config.InstitutionContext;
import com.bank.institutions.Institution;
import com.bank.users.*;
import com.bank.institutions.InstitutionStatus;
import com.bank.others.exceptions.DuplicateResourceException;
import com.bank.others.exceptions.InvalidRequestException;
import com.bank.others.exceptions.UnauthorizedException;
import com.bank.institutions.InstitutionMapper;
import com.bank.institutions.InstitutionRepository;
import com.bank.institutions.RegisterInstitutionRequest;
import com.bank.others.securities.JwtService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuthenticationManager authenticationManager;
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
        institution.setEmailVerifiedAt(LocalDateTime.now());
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
        final String institutionId = InstitutionContext.getCurrentInstitution();

        log.info("Creating user for institution: {}", institutionId);
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
        final User newUser = userMapper.toEntity(registerUserRequest);
        newUser.setInstitution(Institution.builder().id(institutionId).build());

        final User user = userMapper.toEntity(registerUserRequest);
        String emailVerificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(newUser);
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
        if (!passwordEncoder.matches(verificationTokenFromRequest, user.getEmailVerificationToken())) {
            log.debug("Invalid token");
            throw new InvalidRequestException("Invalid token");
        }
        if (passwordEncoder.matches(verificationTokenFromRequest, user.getEmailVerificationToken()) && user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            log.debug("Token has expired");
            throw new InvalidRequestException("Token has expired");
        }
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setIsVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setEmailVerificationToken("used");
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
        log.info("User verified successfully");
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
    public LoginResponse login(final LoginRequest request) throws MessagingException {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        if (user.getIsVerified() == false) {
            log.debug("User not verified");
            throw new InvalidRequestException("User not verified");
        }

        Institution institution = institutionRepository.findById(user.getInstitutionId())
                .orElseThrow(() -> new InvalidRequestException("Institution not found"));
        if (institution.getInstitutionStatus() == InstitutionStatus.SUSPENDED) {
            throw new InvalidRequestException("Your institution has been suspended. Contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.debug("Incorrect password");
            throw new InvalidRequestException("Incorrect password");
        }
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(passwordEncoder.encode(token));
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        log.info("User logged in successfully");

        final User users = (User) authentication.getPrincipal();
        final String accessToken = jwtService.generateAccessToken(users.getInstitutionId(), users.getId(),
                users.getUserAccountType().name());
        final String refreshToken = jwtService.generateRefreshToken(users.getInstitutionId(),
                users.getId(), users.getUserAccountType().name());
        final String tokenType = "Bearer";

        Map<String, Object> model = new HashMap<>();
        model.put("name", request.getUsername());
        model.put("resetUrl", "https://multitenantbank.com/api/v1/auth/reset-password?token=" + token);
        model.put("revokeUrl", "https://multitenantbanking.com/api/v1/auth/revoke-session?token=" + accessToken);

        emailService.sendVerificationEmail(
                request.getUsername(),
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
                .tokenType(tokenType)
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
        UserSession userSession = userSessionRepository
                .findByAccessToken(token)
                .orElseThrow(() -> new InvalidRequestException("Session ended"));
        Instant expiryDate = jwtService.extractExpiration(token).toInstant();

        LogoutToken logoutToken = LogoutToken.builder()
                .token(token)
                .expiryDate(expiryDate)
                .userSession(userSession)
                .build();
        logoutTokenRepository.save(logoutToken);

        userSession.setRevoked(true);
        userSessionRepository.save(userSession);
    }


    @Override
    public void revokeSession(String accessToken) {
        UserSession session = userSessionRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new InvalidRequestException("Session ended"));
        session.setRevoked(true);
        session.setAccessToken("used");
        userSessionRepository.save(session);
    }
}
