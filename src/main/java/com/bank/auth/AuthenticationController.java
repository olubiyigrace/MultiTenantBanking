package com.bank.auth;

import com.bank.auth.requests.LoginRequest;
import com.bank.auth.response.LoginResponse;
import com.bank.auth.service.AuthenticationService;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.services.InstitutionService;
import com.bank.utils.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {
        final LoginResponse response = this.authenticationService.login(request);
        return ResponseEntity.ok(response);
    }
}
