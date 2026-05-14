package com.bank.auth.service;

import com.bank.auth.requests.LoginRequest;
import com.bank.auth.response.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(final LoginRequest request);
}
