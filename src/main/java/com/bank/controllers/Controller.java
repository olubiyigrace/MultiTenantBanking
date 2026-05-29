package com.bank.controllers;


import com.bank.services.LoanApplicationService;
import com.bank.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class Controller {
    private final LoanApplicationService loanApplicationService;


    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'LOAN_OFFICER')")
    @PostMapping("/review-loan-applications")
    public ResponseEntity<ApiResponse<String>> review(@RequestParam String loanApplicationId) {
        loanApplicationService.reviewLoanApplication(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan application is now under review",
                null));
    }
}
