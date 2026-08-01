package com.bank.services;

import com.bank.dtos.requestDtos.ChangePasswordRequest;
import com.bank.dtos.responseDtos.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {
    void changePassword(ChangePasswordRequest request);
    void logout(HttpServletRequest request);
    List<SessionResponse> getActiveSessions(HttpServletRequest request);
    void revokeSession(String token);
    void logoutAllDevices();
}
