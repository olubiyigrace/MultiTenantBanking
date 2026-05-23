package com.bank.controllers;

import com.bank.requests.SavingsAccountRequest;
import com.bank.services.SavingsService;
import com.bank.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
