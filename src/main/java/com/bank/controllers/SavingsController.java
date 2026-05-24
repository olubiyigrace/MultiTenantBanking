package com.bank.controllers;

import com.bank.requests.SavingsAccountRequest;
import com.bank.responses.TotalLoansOutstandingResponse;
import com.bank.responses.TotalLoansOverdueResponse;
import com.bank.responses.TotalSavingsResponse;
import com.bank.services.SavingsService;
import com.bank.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/savings-account")
public class SavingsController {
    private final SavingsService savingsService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> create(@Valid @RequestBody SavingsAccountRequest savingsAccountRequest){
        savingsService.create(savingsAccountRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account created successfully", null));
    }

    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activate(@RequestParam String savingsId){
        savingsService.activateAccount(savingsId);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account activated successfully", null));
    }

    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @PostMapping("/freeze")
    public ResponseEntity<ApiResponse<String>> freeze(@RequestParam String savingsId){
        savingsService.freezeAccount(savingsId);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account frozen successfully", null));
    }

    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @PostMapping("/close")
    public ResponseEntity<ApiResponse<String>> close(@RequestParam String savingsId){
        savingsService.closeAccount(savingsId);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account closed successfully", null));
    }

    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @GetMapping("/summary")
    public ResponseEntity<TotalSavingsResponse> getSavingsSummary() {
        return ResponseEntity.ok(savingsService.getTotalSavings());
    }

    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @GetMapping("/outstanding-loans")
    public ResponseEntity<TotalLoansOutstandingResponse> getTotalLoansOutstanding() {
        return ResponseEntity.ok(savingsService.getTotalLoansOutstanding());
    }

    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @GetMapping("/overdue-loans")
    public ResponseEntity<TotalLoansOverdueResponse> getTotalLoansOverdue() {
        return ResponseEntity.ok(savingsService.getTotalLoansOverdue());
    }


}
