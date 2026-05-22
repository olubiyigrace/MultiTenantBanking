package com.bank.controllers;

import com.bank.requests.LoanProductRequest;
import com.bank.services.LoanProductService;
import com.bank.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@PreAuthorize("hasRole('INSTITUTION_ADMIN')")
public class LoanProductController {
    private final LoanProductService loanProductService;

    @PostMapping("/create-product")
    public ResponseEntity<ApiResponse<String>> create(LoanProductRequest loanProductRequest){
        loanProductService.create(loanProductRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product created successfully", null));
    }
}
