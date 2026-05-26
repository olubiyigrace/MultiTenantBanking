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
@RequestMapping("/api/v1")
public class Controller {
    private final SavingsService savingsService;

    @PostMapping("/create-savings-account")
    public ResponseEntity<ApiResponse<String>> create(@Valid @RequestBody SavingsAccountRequest savingsAccountRequest){
        savingsService.create(savingsAccountRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account created successfully", null));
    }


}
