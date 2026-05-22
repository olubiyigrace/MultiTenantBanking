package com.bank.controllers;

import com.bank.common.PageResponse;
import com.bank.requests.LoanProductRequest;
import com.bank.responses.LoanProductResponse;
import com.bank.services.LoanProductService;
import com.bank.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@PreAuthorize("hasRole('INSTITUTION_ADMIN')")
public class LoanProductController {
    private final LoanProductService loanProductService;

    @PostMapping("/create-products")
    public ResponseEntity<ApiResponse<String>> create(@Valid @RequestBody LoanProductRequest loanProductRequest){
        loanProductService.create(loanProductRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product created successfully", null));
    }

    @GetMapping("/get-single-product")
    public ResponseEntity<ApiResponse<String>> getSingle(String id){
        loanProductService.getSingle(id);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product retrieved successfully", null));
    }

    @GetMapping("/get-all-products")
    public ResponseEntity<PageResponse<LoanProductResponse>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "10") final int size) {
        return ResponseEntity.ok(loanProductService.getAll(page, size));
    }

    @PutMapping("/update-product")
    public ResponseEntity<ApiResponse<String>> update(String id, LoanProductRequest loanProductRequest){
        loanProductService.update(id, loanProductRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product updated successfully", null));
    }

    @GetMapping("/delete-product")
    public ResponseEntity<ApiResponse<String>> delete(String id){
        loanProductService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product deleted successfully", null));
    }

}
