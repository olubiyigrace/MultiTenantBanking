package com.bank.controllers;

import com.bank.dtos.responseDtos.LoanProductResponse;
import com.bank.services.CloudinaryService;
import com.bank.dtos.requestDtos.LoanApplicationRequest;
import com.bank.dtos.requestDtos.LoanCollateralRequest;
import com.bank.services.CollateralService;
import com.bank.services.LoanApplicationService;
import com.bank.utils.ApiResponse;
import com.bank.dtos.requestDtos.GuarantorRequest;
import com.bank.services.GuarantorService;
import com.bank.dtos.requestDtos.SavingsAccountRequest;
import com.bank.services.SavingsService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@PreAuthorize("hasRole('MEMBER')")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MemberController {
    private final LoanApplicationService loanApplicationService;
    private final CollateralService collateralService;
    private final CloudinaryService cloudinaryService;
    private final GuarantorService guarantorService;
    private final SavingsService savingsService;


    @PostMapping("/select-loan-product")
    public ResponseEntity<ApiResponse<List<LoanProductResponse>>> selectLoanProduct(){
        loanApplicationService.getEligibleLoanProducts();
        return ResponseEntity.ok(ApiResponse.success(true, "loan products retrieved successfully", null));
    }

    @PostMapping("/create-loan-application")
    public ResponseEntity<ApiResponse<String>> create(@Valid @RequestBody LoanApplicationRequest loanApplicationRequest){
        loanApplicationService.createApplication(loanApplicationRequest);
        return ResponseEntity.ok(ApiResponse.success(true,"Loan application created successfully! " +
                "If selected loan product requires a collateral or a guarantor, or if it requires both collateral and " +
                "guarantor, add them within a period of 7 days for your loan application to be reviewed.", null));
    }

    @PostMapping("/create-savings-account")
    public ResponseEntity<ApiResponse<String>> createNewSavingsAccount(@Valid @RequestBody SavingsAccountRequest savingsAccountRequest){
        savingsService.createAnotherSavingsAccount(savingsAccountRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account created and added successfully", null));
    }

    @PostMapping("/files-upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.uploadFile(file);
        return ResponseEntity.ok(ApiResponse.success(true, "File uploaded successfully", Map.of("url", url)));
    }

    @PostMapping("/add-collateral")
    public ResponseEntity<ApiResponse<String>> createCollateral(@Valid @RequestBody LoanCollateralRequest loanCollateralRequest){
        collateralService.createCollateral(loanCollateralRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Collateral added successfully", null));
    }

    @PostMapping("/add-guarantor")
    public ResponseEntity<ApiResponse<String>> createGuarantor(@Valid @RequestBody GuarantorRequest guarantorRequest) throws MessagingException {
        guarantorService.createGuarantor(guarantorRequest);
        return ResponseEntity.ok(ApiResponse.success(true,"Guarantor added successfully!", null));
    }

    @PostMapping("/accept-guarantor-request")
    public ResponseEntity<ApiResponse<String>> accept(@RequestParam String loanApplicationId){
        guarantorService.acceptGuarantorRequest(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(true,"guarantor request accepted successfully", null));
    }

    @PostMapping("/reject-guarantor-request")
    public ResponseEntity<ApiResponse<String>> reject(@RequestParam String loanApplicationId){
        guarantorService.rejectGuarantorRequest(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(true,"guarantor request rejected successfully", null));
    }
}
