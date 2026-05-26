package com.bank.controllers;

import com.bank.requests.LoanApplicationRequest;
import com.bank.services.LoanApplicationService;
import com.bank.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasRole('MEMBER')")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MemberController {
    private final LoanApplicationService loanApplicationService;

    @PostMapping("/create-application")
    public ResponseEntity<ApiResponse<String>> create(LoanApplicationRequest loanApplicationRequest){
        loanApplicationService.createApplication(loanApplicationRequest);
        return ResponseEntity.ok(ApiResponse.success(true,"Loan application created successfully", null));
    }
}
