package com.bank.controllers;


import com.bank.dtos.requestDtos.ChangePasswordRequest;
import com.bank.dtos.responseDtos.SessionResponse;
import com.bank.services.AuthenticationService;
import com.bank.services.UserService;
import com.bank.utils.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTION_AMDIN','LOAN_OFFICER', 'ACCOUNTANT', 'MEMBER')")
public class UserController {
    private  final UserService userService;


    @PostMapping("/change-password") // working
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Password changed successfully", null));
    }

    @PostMapping("/logout") // working
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Logout successful", null));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices() {
        userService.logoutAllDevices();
        return ResponseEntity.ok(ApiResponse.success(true, "You have successfully logged out on all devices!", null));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getSessions(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(true, "Sessions retrieved successfully", userService.getActiveSessions(request)));
    }

    @PostMapping("/revoke-session")
    public ResponseEntity<ApiResponse<String>> revokeSession(@RequestParam String sessionId) {
        userService.revokeSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Session revoked successfully", null));
    }
}
