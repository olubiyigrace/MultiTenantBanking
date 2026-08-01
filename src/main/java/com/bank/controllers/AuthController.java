package com.bank.controllers;

import com.bank.dtos.requestDtos.*;
import com.bank.dtos.responseDtos.LoginResponse;
import com.bank.dtos.responseDtos.ResetOtpResponse;
import com.bank.dtos.responseDtos.SelectInstitutionResponse;
import com.bank.services.AuthenticationService;
import com.bank.utils.ApiResponse;
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
    public ResponseEntity<ApiResponse<String>> registerInstitution(@Valid @RequestBody final RegisterInstitutionRequest registerInstitutionRequest){
        authenticationService.registerInstitution(registerInstitutionRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Almost there! Check your email to complete your registration.", null));
    }

    @PostMapping("/verify-inst-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        authenticationService.verifyInstOtp(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Verification successful", null));
    }
    @PostMapping("/verify-user-otp")
    public ResponseEntity<ApiResponse<String>> verifyUserOtp(@Valid @RequestBody OtpVerifyRequest request) {
        authenticationService.verifyUserOtp(request);
        return ResponseEntity.ok(ApiResponse.success(true, "Verification successful", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(true, "OTP sent successfully", null));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@Valid @RequestBody OtpRequest request) {
        authenticationService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.success(true, "OTP sent successfully.", null));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<ResetOtpResponse>> verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        String resetToken = authenticationService.verifyResetOtp(request);
        return ResponseEntity.ok(ApiResponse.success(true, "OTP verified successfully",
                ResetOtpResponse.builder().resetToken(resetToken).build()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword
            (@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody ResetPasswordRequest request) {
        String resetToken = authorizationHeader.substring(7);
        authenticationService.resetPassword(resetToken, request);
        return ResponseEntity.ok(ApiResponse.success(true, "Password reset successfully", null));
    }

    @PostMapping("/pre-login")
    public ResponseEntity<ApiResponse<SelectInstitutionResponse>> preLogin(@Valid @RequestBody final SelectInstitutionRequest request){
        final SelectInstitutionResponse response = authenticationService.preLogin(request);
        return ResponseEntity.ok(ApiResponse.success(true, "login successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest){
        final LoginResponse response = authenticationService.login(request, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "login successful", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Object>> sendRefreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        LoginResponse tokenPair = authenticationService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(true,
                "Token generated successfully", tokenPair));
    }
}
