package com.bank.controllers;

import com.bank.requests.LoginRequest;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.services.InstitutionService;
import com.bank.utils.ApiResponse;
import com.bank.utils.TokenPair;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/auth")
@RestController
@RequiredArgsConstructor
@Slf4j
//POST /auth/register-user
//POST /auth/login
//POST /auth/refresh-token
//POST /auth/change-password
public class InstitutionController {
    private final InstitutionService institutionService;

    @PostMapping("/register-institution")
    public ResponseEntity<ApiResponse<String>> registerInstitution(@Valid @RequestBody final RegisterInstitutionRequest  registerInstitutionRequest) throws MessagingException {
        institutionService.registerInstitution(registerInstitutionRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Almost there! Check your email to complete your registration.", null));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyUser(@RequestParam final String verificationTokenFromRequest, @RequestParam final String email) {
        institutionService.verifyEmail(verificationTokenFromRequest, email);
        return ResponseEntity.ok(ApiResponse.success(true, "Registration completed!", null));

    }
    @GetMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendUserVerificationEmail(@RequestParam final String email) {
        institutionService.resendEmailVerificationToken(email);
        return ResponseEntity.ok(ApiResponse.success(true, "Resent! Check your email to complete your registration.", null));

    }
//    @PostMapping("/login")
//    public ResponseEntity<TokenPair> login(@RequestBody final LoginRequest loginRequest) throws MessagingException {
//        TokenPair tokenPair = institutionService.adminLogin(loginRequest);
//        return ResponseEntity.ok(tokenPair);
//    }
//    @PostMapping("/login")
//    public ResponseEntity<ApiResponse<Object>> login(@RequestBody LoginRequest loginRequest) throws MessagingException {
//        TokenPair tokenPair = userService.userLogin(loginRequest);
//        return ResponseEntity.ok(ApiResponse.success(tokenPair));
//    }
}
