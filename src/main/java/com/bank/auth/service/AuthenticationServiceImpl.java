package com.bank.auth.service;

import com.bank.auth.requests.LoginRequest;
import com.bank.auth.requests.RefreshTokenRequest;
import com.bank.auth.response.LoginResponse;
import com.bank.config.InstitutionContext;
import com.bank.entities.Institution;
import com.bank.entities.User;
import com.bank.enums.UserAccountType;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.InvalidRequestException;
import com.bank.mapper.UserMapper;
import com.bank.repositories.UserRepository;
import com.bank.requests.RegisterUserRequest;
import com.bank.securities.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public LoginResponse login(final LoginRequest request) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        final User user = (User) authentication.getPrincipal();

        final String accessToken = jwtService.generateAccessToken(
                user.getId(),user.getInstitutionId(),
                user.getUserAccountType().name());
        final String refreshToken = jwtService.generateRefreshToken(user.getInstitutionId(),
                user.getId(), user.getUserAccountType().name());
        final String tokenType = "Bearer";

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .build();
    }

    @Override
    public void createUser(RegisterUserRequest registerUserRequest) {
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
    }

    public LoginResponse refreshToken(final RefreshTokenRequest refreshTokenRequest) {
        final String refreshToken = refreshTokenRequest.getRefreshToken();
        if (!jwtService.isRefreshToken(refreshToken)) {
            log.debug("Invalid refresh token!");
            throw new InvalidRequestException("Invalid refresh token!");
        }
        if (jwtService.validateToken(refreshToken)) {
            log.debug("Refresh token has expired!");
            throw new InvalidRequestException("Refresh token has expired!");
        }
        final String userId = jwtService.getUserIdFromToken(refreshToken);
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        final String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getInstitutionId(),
                user.getUserAccountType().name()
        );
        final String tokenType = "Bearer";
        return new LoginResponse(
                accessToken,
                refreshToken,
                tokenType
        );
    }
}
