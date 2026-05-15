package com.bank.auth.service;

import com.bank.auth.requests.LoginRequest;
import com.bank.auth.requests.RefreshTokenRequest;
import com.bank.auth.response.LoginResponse;
import com.bank.requests.RegisterUserRequest;

public interface AuthenticationService {
    LoginResponse login(final LoginRequest request);
    void createUser(RegisterUserRequest registerUserRequest);
    LoginResponse refreshToken(final RefreshTokenRequest refreshTokenRequest);
}
