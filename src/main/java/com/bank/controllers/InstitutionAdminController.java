package com.bank.controllers;

import com.bank.auth.requests.RegisterUserRequest;
import com.bank.auth.response.UserResponse;
import com.bank.auth.service.AuthenticationService;
import com.bank.responses.PageResponse;
import com.bank.enums.ProfileStatus;
import com.bank.requests.LoanProductRequest;
import com.bank.responses.*;
import com.bank.services.InstitutionService;
import com.bank.services.LoanProductService;
import com.bank.services.MemberService;
import com.bank.services.SavingsService;
import com.bank.utils.ApiResponse;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.time.Year;

@PreAuthorize("hasRole('INSTITUTION_ADMIN')")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InstitutionAdminController {
    private final LoanProductService loanProductService;
    private final MemberService memberService;
    private final SavingsService savingsService;
    private final AuthenticationService authenticationService;
    private final InstitutionService institutionService;

    @PostMapping("/register-user")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody final RegisterUserRequest registerUserRequest) throws MessagingException {
        authenticationService.createUser(registerUserRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Almost there! Check your email to complete your registration.", null));
    }

    @GetMapping("/all-users")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "10") final int size) {
        return ResponseEntity.ok(institutionService.getAllUsers(page, size));
    }

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

    @GetMapping("/all-members")
    public ResponseEntity<ApiResponse<PageResponse<MemberResponse>>> getMembers(
            @RequestParam ProfileStatus profileStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10")  int size) {
        return ResponseEntity.ok(ApiResponse.success(true, "Members retrieved successfully",
                memberService.getAllMembers(profileStatus, page, size)));
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

    @GetMapping("/total-savings")
    public ResponseEntity<TotalSavingsResponse> getSavingsSummary() {
        return ResponseEntity.ok(savingsService.getTotalSavings());
    }

    @GetMapping("/total-outstanding-loans")
    public ResponseEntity<TotalLoansOutstandingResponse> getTotalLoansOutstanding() {
        return ResponseEntity.ok(savingsService.getTotalLoansOutstanding());
    }

    @GetMapping("/total-overdue-loans")
    public ResponseEntity<TotalLoansOverdueResponse> getTotalLoansOverdue() {
        return ResponseEntity.ok(savingsService.getTotalLoansOverdue());
    }

    @GetMapping("/total-interest")
    public ResponseEntity<TotalInterestCollectedResponse> getTotalInterest(
            @RequestParam(value = "month", required = false) final Month month,
            @RequestParam(value = "year", required = false) final Year year) {
        return ResponseEntity.ok(savingsService.getTotalInterestCollected(month, year));
    }
}
