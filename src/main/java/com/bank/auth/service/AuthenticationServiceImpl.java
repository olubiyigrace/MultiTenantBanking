package com.bank.auth.service;

import com.bank.auth.requests.*;
import com.bank.auth.response.LoginResponse;
import com.bank.auth.util.CurrentUserUtil;
import com.bank.config.InstitutionContext;
import com.bank.entities.Institution;
import com.bank.entities.User;
import com.bank.enums.InstitutionStatus;
import com.bank.enums.UserAccountType;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.InvalidRequestException;
import com.bank.exceptions.UnauthorizedException;
import com.bank.mapper.InstitutionMapper;
import com.bank.mapper.UserMapper;
import com.bank.repositories.InstitutionRepository;
import com.bank.repositories.UserRepository;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.requests.RegisterUserRequest;
import com.bank.securities.JwtService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;
    private final com.bank.services.EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserUtil currentUserUtil;


    @Override
    @Transactional
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
                "verification",
                model
        );
    }

    @Override
    @Transactional
    public void verifyEmail(final String verificationTokenFromRequest, final String email) {
        Institution institution = institutionRepository.findByInstitutionEmail(email)
                .orElseThrow(() -> new InvalidRequestException("Institution with the email '" + email + "' does not exist. Visit the website to register"));
        if (!passwordEncoder.matches(verificationTokenFromRequest, institution.getEmailVerificationToken())) {
            log.debug("Invalid token");
            throw new InvalidRequestException("Invalid token");
        }
        if (institution.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            log.debug("Token has expired");
            throw new RuntimeException("Token has expired");
        }
        institution.setEmailVerifiedAt(LocalDateTime.now());
        institution.setIsVerified(true);
        institution.setEmailVerifiedAt(LocalDateTime.now());
        institution.setEmailVerificationToken("used");
        institution.setEmailVerificationTokenExpiry(null);
        institutionRepository.save(institution);
    }

    @Override
    @Transactional
    public void resendEmailVerificationToken(final String email) {
        Institution institution = institutionRepository.findByInstitutionEmail(email)
                .orElseThrow(() -> new UnauthorizedException("user with the email '" + email + "'  does not exist. Visit the website to create an account."));

        if (Boolean.TRUE.equals(institution.getIsVerified())) {
            log.debug("User already verified");
            throw new DuplicateResourceException("User already verified");
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
                    "verification",
                    model
            );
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void createUser(RegisterUserRequest registerUserRequest) throws MessagingException {
        final String institutionId = InstitutionContext.getCurrentInstitution();
        log.info("Creating user for institution: {}", institutionId);
        if(userRepository.existsByEmail(registerUserRequest.getEmail())){
            log.debug("User with the email '{}' already exists.", registerUserRequest.getEmail());
            throw new DuplicateResourceException("User with the email '" + registerUserRequest.getEmail() + "' already exists.");
        }
        if(userRepository.existsByUsername(registerUserRequest.getEmail())){
            log.debug("User with the username '{}' already exists.", registerUserRequest.getEmail());
            throw new DuplicateResourceException("User with the username '" + registerUserRequest.getEmail() + "' already exists.");
        }
        if(registerUserRequest.getUserAccountType() == UserAccountType.SUPER_ADMIN || registerUserRequest.getUserAccountType() == UserAccountType.INSTITUTION_ADMIN
        ){throw new InvalidRequestException("SUPER_ADMIN or INSTITUTION_ADMIN cannot be selected as an account type");
        }
        final User newUser = userMapper.toEntity(registerUserRequest);
        newUser.setInstitution(Institution.builder().id(institutionId).build());
        userRepository.save(newUser);
        log.info("User created successfully!");

        final User user = userMapper.toEntity(registerUserRequest);
        String emailVerificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));

        Map<String, Object> model = new HashMap<>();
        model.put("institutionName", registerUserRequest.getName());
        model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/verify?token=" + emailVerificationToken);

        emailService.sendVerificationEmail(
                registerUserRequest.getEmail(),
                "Verify your account",
                "verification",
                model
        );
    }

    @Override
    @Transactional
    public void verifyUser(final String verificationTokenFromRequest, final String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidRequestException("User with the email '" + email + "' does not exist. Visit the website to register"));
        if (!passwordEncoder.matches(verificationTokenFromRequest, user.getEmailVerificationToken())) {
            log.debug("Invalid token");
            throw new InvalidRequestException("Invalid token");
        }
        if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            log.debug("Token has expired");
            throw new RuntimeException("Token has expired");
        }
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setIsVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setEmailVerificationToken("used");
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendUserVerificationToken(final String email) {
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
    @Transactional
    public LoginResponse login(final LoginRequest request) throws MessagingException {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        if (user.getIsVerified() == false) throw new InvalidRequestException("User not verified");

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

        Map<String, Object> model = new HashMap<>();
        model.put("name", request.getUsername());
        model.put("resetUrl", "https://multitenantbank.com/api/v1/auth/reset-password?token=" + token);

        emailService.sendVerificationEmail(
                request.getUsername(),
                "New Login Alert!",
                "login",
                model
        );
        final User users = (User) authentication.getPrincipal();
        final String accessToken = jwtService.generateAccessToken(users.getInstitutionId(), users.getId(),
                users.getUserAccountType().name());
        final String refreshToken = jwtService.generateRefreshToken(users.getInstitutionId(),
                users.getId(), users.getUserAccountType().name());
        final String tokenType = "Bearer";
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .build();
    }


    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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

}
