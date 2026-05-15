package com.bank.auth.service;

import com.bank.auth.requests.LoginRequest;
import com.bank.auth.response.LoginResponse;
import com.bank.entities.User;
import com.bank.securities.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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
}
