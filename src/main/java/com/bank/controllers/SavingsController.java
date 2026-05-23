package com.bank.controllers;

import com.bank.requests.SavingsAccountRequest;
import com.bank.services.SavingsService;
import com.bank.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activate(@RequestParam String savingsId){
        savingsService.activateAccount(savingsId);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account activated successfully", null));
    }

    @PostMapping("/freeze")
    public ResponseEntity<ApiResponse<String>> freeze(@RequestParam String savingsId){
        savingsService.freezeAccount(savingsId);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account frozen successfully", null));
    }
    @PostMapping("/close")
    public ResponseEntity<ApiResponse<String>> close(@RequestParam String savingsId){
        savingsService.closeAccount(savingsId);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account closed successfully", null));
    }
}
