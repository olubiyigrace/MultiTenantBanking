package com.bank.auth;

import com.bank.auth.requests.LoginRequest;
import com.bank.auth.requests.RefreshTokenRequest;
import com.bank.auth.response.LoginResponse;
import com.bank.auth.service.AuthenticationService;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.requests.RegisterUserRequest;
import com.bank.services.InstitutionService;
import com.bank.services.UserService;
import com.bank.utils.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
//POST /auth/change-password
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final InstitutionService institutionService;

    @PostMapping("/register-institution")
    public ResponseEntity<ApiResponse<String>> registerInstitution(@Valid @RequestBody final RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException {
        institutionService.registerInstitution(registerInstitutionRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Almost there! Check your email to complete your registration.", null));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyInstitution(@RequestParam final String verificationTokenFromRequest, @RequestParam final String email) {
        institutionService.verifyEmail(verificationTokenFromRequest, email);
        return ResponseEntity.ok(ApiResponse.success(true, "Registration completed!", null));
    }
    @GetMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendUserVerificationEmail(@RequestParam final String email) {
        institutionService.resendEmailVerificationToken(email);
        return ResponseEntity.ok(ApiResponse.success(true, "Resent! Check your email to complete your registration.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
        final LoginResponse response = this.authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-user")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody final RegisterUserRequest registerUserRequest){
        authenticationService.createUser(registerUserRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "User registered successfully!", null));
    }

    @PostMapping("/refresh-tokennnnn")
    public ResponseEntity<ApiResponse<Object>> sendRefreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        LoginResponse tokenPair = authenticationService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(tokenPair));
    }

}
