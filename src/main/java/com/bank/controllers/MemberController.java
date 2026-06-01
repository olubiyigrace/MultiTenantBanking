package com.bank.controllers;

import com.bank.others.services.CloudinaryService;
import com.bank.loanapplications.LoanApplicationRequest;
import com.bank.loancollaterals.LoanCollateralRequest;
import com.bank.loancollaterals.CollateralService;
import com.bank.loanapplications.LoanApplicationService;
import com.bank.others.utils.ApiResponse;
import com.bank.loanguarantors.GuarantorRequest;
import com.bank.loanguarantors.GuarantorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/create-loan-application")
    public ResponseEntity<ApiResponse<String>> create(LoanApplicationRequest loanApplicationRequest){
        loanApplicationService.createApplication(loanApplicationRequest);
        return ResponseEntity.ok(ApiResponse.success(true,"Loan application created successfully! " +
                "If selected loan product requires a collateral or a guarantor, or if it requires both collateral and " +
                "guarantor, add them now for your loan application to be reviewed.", null));
    }

    @PostMapping("/add-collateral")
    public ResponseEntity<ApiResponse<String>> createCollateral(@Valid @RequestBody LoanCollateralRequest loanCollateralRequest){
        collateralService.createCollateral(loanCollateralRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Collateral added successfully", null));
    }

    @PostMapping("/files-upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.uploadFile(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/create-guarantor")
    public ResponseEntity<ApiResponse<String>> createGuarantor(GuarantorRequest guarantorRequest){
        guarantorService.createGuarantor(guarantorRequest);
        return ResponseEntity.ok(ApiResponse.success(true,"Guarantor added successfully!", null));
    }
}
