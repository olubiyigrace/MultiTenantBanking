package com.bank.controllers;

import com.bank.others.utils.ApiResponse;
import com.bank.loanapplications.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@PreAuthorize("hasRole('ACCOUNTANT')")
public class AccountantController {
    private final LoanApplicationService loanApplicationService;

    @PostMapping("/disburse-loan")
    public ResponseEntity<ApiResponse<String>> disburse(@RequestParam String loanApplicationId){
        loanApplicationService.disburseLoan(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan disbursed successfully", null));
    }
}
