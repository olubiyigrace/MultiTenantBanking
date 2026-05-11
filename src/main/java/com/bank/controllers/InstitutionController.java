package com.bank.controllers;

import com.bank.dto.RegisterInstitutionRequest;
import com.bank.services.InstitutionService;
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
    public ResponseEntity<Void> registerInstitution(@Valid @RequestBody final RegisterInstitutionRequest  registerInstitutionRequest) throws MessagingException {
        institutionService.registerInstitution(registerInstitutionRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyUser(@RequestParam String verificationTokenFromRequest, @RequestParam String email) {
        institutionService.verifyEmail(verificationTokenFromRequest, email);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/resend-verification")
    public ResponseEntity<Void> resendUserVerificationEmail(@RequestParam String email) {
        institutionService.resendEmailVerificationToken(email);
        return ResponseEntity.ok().build();
    }
}
