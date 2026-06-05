package com.bank.controllers;

import com.bank.others.login.LoginRequest;
import com.bank.others.password.ChangePasswordRequest;
import com.bank.others.password.ForgotPasswordRequest;
import com.bank.others.password.ResetPasswordRequest;
import com.bank.others.login.LoginResponse;
import com.bank.others.auth.AuthenticationService;
import com.bank.others.auth.RefreshTokenRequest;
import com.bank.institutions.RegisterInstitutionRequest;
import com.bank.others.utils.ApiResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register-institution")
    public ResponseEntity<ApiResponse<String>> registerInstitution(
            @Valid @RequestBody final RegisterInstitutionRequest registerInstitutionRequest)
            throws MessagingException {
        authenticationService.registerInstitution(registerInstitutionRequest);
        return ResponseEntity.ok(ApiResponse.success(true,
                "Almost there! Check your email to complete your registration.", null));
    }

    @GetMapping("/verify-institution")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam final String verificationTokenFromRequest,
                                                           @RequestParam final String email) {
        authenticationService.verifyEmail(verificationTokenFromRequest, email);
        return ResponseEntity.ok(ApiResponse.success(true, "Institution registered successfully",
                null));
    }

    @GetMapping("/reverify-institution")
    public ResponseEntity<ApiResponse<String>> resendInstitutionVerificationEmail(@RequestParam final String email) {
        authenticationService.reverifyInstitutionEmail(email);
        return ResponseEntity.ok(ApiResponse.success(true,
                "Resent! Check your email to complete your registration.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody final LoginRequest request)
            throws MessagingException {
        final LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success(true, "login successful", response));
    }

    @GetMapping("/verify-user")
    public ResponseEntity<ApiResponse<String>> verifyUser(@RequestParam final String verificationTokenFromRequest,
                                                          @RequestParam final String email) {
        authenticationService.verifyUser(verificationTokenFromRequest, email);
        return ResponseEntity.ok(ApiResponse.success(true, "User registered successfully", null));
    }

    @GetMapping("/reverify-user")
    public ResponseEntity<ApiResponse<String>> resendUserVerification(@RequestParam final String email) {
        authenticationService.resendUserVerificationToken(email);
        return ResponseEntity.ok(ApiResponse.success(true,
                "Resent! Check your email to complete your registration.", null));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Object>> sendRefreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        LoginResponse tokenPair = authenticationService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(true,
                "Refresh token sent successfully", tokenPair));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        authenticationService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(ApiResponse.success(true,
                "Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request)
            throws MessagingException {
        authenticationService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Check your email to reset your password",
                null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                                             @RequestParam String token) {
        authenticationService.resetPasswordWithToken(token, request);
        return ResponseEntity.ok(ApiResponse.success(true, "Password reset successful", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Logout successful", null));
    }

    @PostMapping("/revoke-session")
    public ResponseEntity<ApiResponse<String>> revokeSession(@RequestParam String token) {
        authenticationService.revokeSession(token);
        return ResponseEntity.ok(ApiResponse.success(true, "Session revoked successfully", null));
    }
}
