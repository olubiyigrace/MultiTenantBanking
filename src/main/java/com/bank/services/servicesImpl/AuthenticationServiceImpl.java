package com.bank.services.servicesImpl;

import com.bank.dtos.requestDtos.*;
import com.bank.dtos.responseDtos.LoginResponse;
import com.bank.dtos.responseDtos.SelectInstitutionResponse;
import com.bank.dtos.responseDtos.UserInstitutionsResponse;
import com.bank.entities.*;
import com.bank.enums.*;
import com.bank.mappers.InstitutionMapper;
import com.bank.mappers.UserMapper;
import com.bank.orders.OrderProducer;
import com.bank.orders.ProducerMessage;
import com.bank.properties.JwtProperties;
import com.bank.repositories.*;
import com.bank.dtos.requestDtos.ForgotPasswordRequest;
import com.bank.dtos.requestDtos.ResetPasswordRequest;
import com.bank.services.AuthenticationService;
import com.bank.services.OtpService;
import com.bank.services.RedisSessionService;
import com.bank.utils.CurrentUserUtil;
import com.bank.services.EmailService;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.InvalidRequestException;
import com.bank.exceptions.UnauthorizedException;
import com.bank.security.JwtService;
import com.bank.utils.IpAddressUtil;
import com.bank.utils.UserAgentUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
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
    private final MemberRepository memberRepository;
    private final OtpService otpService;
    private final OrderProducer orderProducer;
    private final OtpRepository otpRepository;
    private final AuthenticationManager authenticationManager;
    private final RedisSessionService redisSessionService;
    private final UserAgentUtil userAgentUtil;
    private final IpAddressUtil ipAddressUtil;
    private final JwtProperties jwtProperties;



    @Override
    public void registerInstitution(RegisterInstitutionRequest registerInstitutionRequest){
        if (institutionRepository.existsByInstitutionRcNumber(registerInstitutionRequest.getInstitutionRcNumber())) {
            log.debug("Institution with the RC Number '{}' has been registered.", registerInstitutionRequest.getInstitutionRcNumber());
            throw new DuplicateResourceException("Institution with the RC Number '" + registerInstitutionRequest.getInstitutionRcNumber() + "' already exists..");
        }
        if (institutionRepository.existsByInstitutionEmail(registerInstitutionRequest.getInstitutionEmail())) {
            log.debug("Institution with the email '{}' has been registered.", registerInstitutionRequest.getInstitutionEmail());
            throw new DuplicateResourceException("Institution with the email '" + registerInstitutionRequest.getInstitutionEmail() + "' has been registered.");
        }
        Institution institution = institutionMapper.toEntity(registerInstitutionRequest);
        institution.setInstitutionStatus(InstitutionStatus.PENDING);
        institutionRepository.save(institution);

        String otp = otpService.createOtp(
                OtpRequest.builder()
                        .email(institution.getInstitutionEmail())
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
        orderProducer.sendMessage(
                ProducerMessage.builder()
                        .email(institution.getInstitutionEmail())
                        .otp(otp)
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
    }

    @Override
    public void verifyInstOtp(OtpVerifyRequest request) {
        Optional<Otp> existingOtp = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), request.getPurpose());
        if (existingOtp.isEmpty()) {
            throw new InvalidRequestException("OTP not found");
        }
        boolean isMatch = passwordEncoder.matches(request.getOtp(), existingOtp.get().getOtp());
        if (!isMatch) {
            throw new InvalidRequestException("Invalid OTP");
        }
        if (existingOtp.get().getUsed().equals(true)) {
            throw new DuplicateResourceException("OTP already used");
        }
        if (existingOtp.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("OTP has expired");
        }
        existingOtp.get().setUsed(true);
        Institution institution = institutionRepository.findByInstitutionEmail(request.getEmail());
        institution.setIsVerified(true);
        institutionRepository.save(institution);
        otpRepository.save(existingOtp.get());
    }

    @Override
    public void resendOtp(OtpRequest request) {
        Optional<Otp> existingUser = otpRepository.findByEmailAndPurpose(request.getEmail(), request.getPurpose());
        if(existingUser.isEmpty()){
            throw new InvalidRequestException(request.getEmail() + " not found.");
        }
        String otp = otpService.resendOtp(
                OtpRequest.builder()
                        .email(request.getEmail())
                        .purpose(request.getPurpose())
                        .build()
        );
        orderProducer.sendMessage(
                ProducerMessage.builder()
                        .email(request.getEmail())
                        .otp(otp)
                        .purpose(request.getPurpose())
                        .build()
        );
    }

    @Override
    public void createUser(RegisterUserRequest registerUserRequest){
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
        userRepository.save(user);
        log.info("User created successfully!");

        String otp = otpService.createOtp(
                OtpRequest.builder()
                        .email(user.getEmail())
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
        orderProducer.sendMessage(
                ProducerMessage.builder()
                        .email(user.getEmail())
                        .otp(otp)
                        .purpose(OtpPurpose.VERIFY_ACCOUNT)
                        .build()
        );
    }

    @Override
    public void verifyUserOtp(OtpVerifyRequest request) {
        Optional<Otp> existingOtp = otpRepository.findByEmailAndPurpose(request.getEmail(), request.getPurpose());
        if (existingOtp.isEmpty()) {
            throw new InvalidRequestException("OTP not found");
        }
        boolean isMatch = passwordEncoder.matches(request.getOtp(), existingOtp.get().getOtp());
        if (!isMatch) {
            throw new InvalidRequestException("Invalid OTP");
        }
        if (existingOtp.get().getUsed().equals(true)) {
            throw new DuplicateResourceException("OTP already used");
        }
        if (existingOtp.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("OTP has expired");
        }
        existingOtp.get().setUsed(true);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidRequestException("Invalid email"));
        user.setIsVerified(true);
        userRepository.save(user);
        otpRepository.save(existingOtp.get());
    }

    @Override
    public SelectInstitutionResponse preLogin(SelectInstitutionRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User authenticatedUser = (User) authentication.getPrincipal();
        if (!passwordEncoder.matches(request.getPassword(), authenticatedUser.getPassword())) {
            throw new InvalidRequestException("Invalid credentials");
        }
        if (!Boolean.TRUE.equals(authenticatedUser.getIsVerified())) {
            throw new InvalidRequestException("User not verified");
        }
        if (authenticatedUser.getUserAccountType() == UserAccountType.SUPER_ADMIN) {
            String loginToken = jwtService.generateLoginToken(authenticatedUser.getId());
            return SelectInstitutionResponse.builder()
                    .loginType("SINGLE")
                    .loginToken(loginToken)
                    .institutions(List.of())
                    .build();
        }
        if (authenticatedUser.getUserAccountType() != UserAccountType.MEMBER) {
            Institution institution = institutionRepository.findById(authenticatedUser.getInstitutionId())
                    .orElseThrow(() -> new InvalidRequestException("Institution not found"));
            String loginToken = jwtService.generateLoginToken(authenticatedUser.getId());
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
        List<MemberProfile> profiles = memberRepository.findByUserId(authenticatedUser.getId());
        List<UserInstitutionsResponse> institutions = profiles.stream()
                        .map(p -> new UserInstitutionsResponse(
                                p.getInstitution().getId(),
                                p.getInstitution().getInstitutionName(),
                                p.getInstitution().getInstitutionType(),
                                p.getInstitution().getInstitutionStatus()
                        ))
                        .toList();

        String loginToken = jwtService.generateLoginToken(authenticatedUser.getId());
        return SelectInstitutionResponse.builder()
                .loginType("MULTI")
                .loginToken(loginToken)
                .institutions(institutions)
                .build();
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest){
        jwtService.validateLoginToken(request.getLoginToken());
        String userId = jwtService.getUserIdFromLoginToken(request.getLoginToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Institution institution;
        if (user.getUserAccountType().equals(UserAccountType.SUPER_ADMIN)) {
            String sessionId = UUID.randomUUID().toString();
            UserSession session = UserSession.builder()
                    .sessionId(sessionId)
                    .userId(user.getId())
                    .companyId(user.getInstitutionId())
                    .role(user.getUserAccountType().name())
                    .ipAddress(ipAddressUtil.getClientIp(httpServletRequest))
                    .userAgent(userAgentUtil.getUserAgent(httpServletRequest))
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())))
                    .build();
            redisSessionService.saveSession(session);
            redisSessionService.saveRefreshSession(session);

            Map<String, Object> model = new HashMap<>();
            model.put("name", user.getName());
            model.put("loginTime", session.getCreatedAt());
            model.put("device", session.getUserAgent());
            model.put("ipAddress", session.getIpAddress());
            model.put("institutionName", user.getName());
            model.put("securityUrl", "https://nova.com/settings/security");
            model.put("changePasswordUrl", "https://nova.com/settings/security/password");
            try {
                emailService.sendVerificationEmail(
                        user.getEmail(),
                        "New Login Alert!",
                        "login",
                        model);
            } catch (MessagingException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            String accessToken = jwtService.generateAccessToken(
                    "not_required",
                    user.getId(),
                    sessionId,
                    user.getUserAccountType().name());
            String refreshToken = jwtService.generateRefreshToken(
                   "not_required",
                    user.getId(),
                    sessionId,
                    user.getUserAccountType().name());
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
        String sessionId = UUID.randomUUID().toString();
        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(user.getId())
                .companyId(user.getInstitutionId())
                .role(user.getUserAccountType().name())
                .ipAddress(ipAddressUtil.getClientIp(httpServletRequest))
                .userAgent(userAgentUtil.getUserAgent(httpServletRequest))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())))
                .build();
        redisSessionService.saveSession(session);
        redisSessionService.saveRefreshSession(session);

        Map<String, Object> model = new HashMap<>();
        model.put("name", user.getName());
        model.put("loginTime", session.getCreatedAt());
        model.put("device", session.getUserAgent());
        model.put("ipAddress", session.getIpAddress());
        model.put("securityUrl", "https://nova.com/settings/security");
        model.put("changePasswordUrl", "https://nova.com/settings/security/password");
        try {
            emailService.sendVerificationEmail(
                    user.getEmail(),
                    "New Login Alert!",
                    "login",
                    model);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String accessToken = jwtService.generateAccessToken(
                institution.getId(),
                user.getId(),
                sessionId,
                user.getUserAccountType().name());

        String refreshToken = jwtService.generateRefreshToken(
                institution.getId(),
                user.getId(),
                sessionId,
                user.getUserAccountType().name());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request){
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if(existingUser.isEmpty()){
            throw new InvalidRequestException(request.getEmail() + " not found.");
        }
        String otp = otpService.createOtp(
                OtpRequest.builder()
                        .email(request.getEmail())
                        .purpose(OtpPurpose.RESET_PASSWORD)
                        .build()
        );
        orderProducer.sendMessage(
                ProducerMessage.builder()
                        .email(request.getEmail())
                        .otp(otp)
                        .purpose(OtpPurpose.RESET_PASSWORD)
                        .build()
        );
        log.info("Reset link sent");
    }

    @Override
    public String verifyResetOtp(VerifyResetOtpRequest request) {
        Optional<Otp> existingOtp = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), OtpPurpose.RESET_PASSWORD);
        if (existingOtp.isEmpty()) {
            throw new InvalidRequestException("OTP not found");
        }
        Otp newOtp = existingOtp.get();
        boolean isMatch = passwordEncoder.matches(request.getOtp(), newOtp.getOtp());
        if (!isMatch) {
            throw new InvalidRequestException("Invalid OTP");
        }
        if (newOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("OTP expired");
        }
        newOtp.setUsed(true);
        otpRepository.save(newOtp);
        return jwtService.generatePasswordResetToken(request.getEmail());
    }

    @Override
    public void resetPassword(String token, ResetPasswordRequest request) {
        if(!jwtService.validatePasswordResetToken(token)){
            throw  new InvalidRequestException("Invalid token");
        }
        String email = jwtService.getEmailFromResetToken(token);
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            throw new InvalidRequestException("User not found");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }
        User user = existingUser.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public LoginResponse refreshToken(final RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidRequestException("Invalid refresh token");
        }
        jwtService.validateToken(refreshToken);
        String sessionId = jwtService.getSessionId(refreshToken);
        UserSession session = redisSessionService.getRefreshSession(sessionId);
        if (session == null) {
            throw new InvalidRequestException("Refresh session expired");
        }
        String tokenUserId = jwtService.getUserIdFromRefreshToken(refreshToken);
        if (!session.getUserId().equals(tokenUserId)) {
            throw new UnauthorizedException("Invalid refresh session");
        }
        User user = userRepository.findById(tokenUserId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())));
        redisSessionService.saveSession(session);
        redisSessionService.saveRefreshSession(session);
        final String newAccessToken = jwtService.generateAccessToken(
                user.getInstitutionId(),
                user.getId(),
                sessionId,
                user.getUserAccountType().name());
        final String newRefreshToken = jwtService.generateRefreshToken(
                user.getInstitutionId(),
                user.getId(),
                sessionId,
                user.getUserAccountType().name());
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }
}