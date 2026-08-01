package com.bank.controllers;

import com.bank.dtos.responseDtos.TotalInterestCollectedResponse;
import com.bank.dtos.responseDtos.TotalLoansOutstandingResponse;
import com.bank.dtos.responseDtos.TotalLoansOverdueResponse;
import com.bank.dtos.responseDtos.TotalSavingsResponse;
import com.bank.dtos.responseDtos.LoanApplicationResponse;
import com.bank.dtos.requestDtos.LoanRejectionRequest;
import com.bank.dtos.responseDtos.LoanProductResponse;
import com.bank.dtos.responseDtos.MemberResponse;
import com.bank.dtos.requestDtos.RegisterUserRequest;
import com.bank.services.AuthenticationService;
import com.bank.services.LoanApplicationService;
import com.bank.services.CollateralService;
import com.bank.services.LoanProductService;
import com.bank.services.MemberService;
import com.bank.utils.PageResponse;
import com.bank.enums.ProfileStatus;
import com.bank.dtos.requestDtos.LoanProductRequest;
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
    private final LoanApplicationService loanApplicationService;
    private final CollateralService collateralService;

    @PostMapping("/register-user") // working
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest){
        authenticationService.createUser(registerUserRequest);
        return ResponseEntity.ok(ApiResponse.success(true,
                "Almost there! Check your email to complete your registration.", null));
    }

    @GetMapping("/all-members") // working
    public ResponseEntity<ApiResponse<PageResponse<MemberResponse>>> getMembers(
            @RequestParam (required = false) ProfileStatus profileStatus,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(true, "Members retrieved successfully",
                memberService.getAllMembers(profileStatus, page, size)));
    }

    @PostMapping("/create-loan-products")
    public ResponseEntity<ApiResponse<String>> create(@Valid @RequestBody LoanProductRequest loanProductRequest) {
        loanProductService.create(loanProductRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product created successfully",
                null));
    }

    @GetMapping("/get-single-product")
    public ResponseEntity<ApiResponse<LoanProductResponse>> getSingle(@RequestParam String id) {
        loanProductService.getSingle(id);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product retrieved successfully",
                null));
    }

    @GetMapping("/get-all-products")
    public ResponseEntity<ApiResponse<PageResponse<LoanProductResponse>>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "10") final int size) {
        return ResponseEntity.ok(ApiResponse.success(true, "Products retrieved successfully",
                loanProductService.getAll(page, size)));
    }

    @PutMapping("/update-product")
    public ResponseEntity<ApiResponse<String>> update(@RequestParam String id, @Valid @RequestBody LoanProductRequest loanProductRequest) {
        loanProductService.update(id, loanProductRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product updated successfully", null));
    }

    @GetMapping("/delete-product")
    public ResponseEntity<ApiResponse<String>> delete(@RequestParam String id) {
        loanProductService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product deleted successfully",
                null));
    }

    @PostMapping("/activate-savings-account")
    public ResponseEntity<ApiResponse<String>> activate(@RequestParam String savingsId) {
        savingsService.activateAccount(savingsId);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account activated successfully",
                null));
    }

    @PostMapping("/freeze-savings-account")
    public ResponseEntity<ApiResponse<String>> freeze(@RequestParam String savingsId) {
        savingsService.freezeAccount(savingsId);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account frozen successfully",
                null));
    }

    @PostMapping("/close-savings-account")
    public ResponseEntity<ApiResponse<String>> close(@RequestParam String savingsId) {
        savingsService.closeAccount(savingsId);
        return ResponseEntity.ok(ApiResponse.success(true, "Savings account closed successfully",
                null));
    }

    @GetMapping("/total-savings")
    public ResponseEntity<ApiResponse<TotalSavingsResponse>> getSavingsSummary() {
        return ResponseEntity.ok(ApiResponse.success(true, "Total savings calculated successfully",
                savingsService.getTotalSavings()));
    }

    @PostMapping("/activate-loan-product")
    public ResponseEntity<ApiResponse<String>> activateLoanProduct(@Valid @RequestParam String loanProductId){
        loanProductService.activateLoanProduct(loanProductId);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product activated successfully", null));
    }

    @PostMapping("/deactivate-loan-product")
    public ResponseEntity<ApiResponse<String>> deactivateLoanProduct(@Valid @RequestParam String loanProductId){
        loanProductService.deactivateLoanProduct(loanProductId);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan product deactivated successfully", null));
    }

    @GetMapping("/all-loan-applications")
    public ResponseEntity<ApiResponse<PageResponse<LoanApplicationResponse>>> getAllLoanApplications(
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "10") final int size) {
        return ResponseEntity.ok(ApiResponse.success(true, "Loan applications retrieved successfully",
                loanApplicationService.getAllApplications(page, size)));
    }

    @PostMapping("/review-loan-application")
    public ResponseEntity<ApiResponse<String>> review(@RequestParam String loanApplicationId) {
        loanApplicationService.reviewLoanApplication(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan application is now under review",
                null));
    }

    @PostMapping("/assign-loan-applications")
    public ResponseEntity<ApiResponse<String>> assignApplication(@RequestParam String loanApplicationId,
                                                                 @RequestParam String loanOfficerId) {
        loanApplicationService.assignApplication(loanApplicationId, loanOfficerId);
        return ResponseEntity.ok(ApiResponse.success(true,
                "Loan application successfully assigned to the loan officer", null));
    }

    @PostMapping("/approve-loan-application")
    public ResponseEntity<ApiResponse<String>> approve(@RequestParam String loanApplicationId) {
        loanApplicationService.approveLoan(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan application approved successfully",
                null));
    }

    @PostMapping("/reject-loan-application")
    public ResponseEntity<ApiResponse<String>> reject(@RequestParam String loanApplicationId, @Valid @RequestBody LoanRejectionRequest loanRejectionRequest) {
        loanApplicationService.rejectLoan(loanApplicationId, loanRejectionRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan application rejected successfully",
                null));
    }

    @GetMapping("/total-outstanding-loans")
    public ResponseEntity<ApiResponse<TotalLoansOutstandingResponse>> getTotalLoansOutstanding() {
        return ResponseEntity.ok(ApiResponse.success(true,
                "Total outstanding loans calculated successfully", savingsService.getTotalLoansOutstanding()));
    }

    @GetMapping("/total-overdue-loans")
    public ResponseEntity<ApiResponse<TotalLoansOverdueResponse>> getTotalLoansOverdue() {
        return ResponseEntity.ok(ApiResponse.success(true,
                "Total overdue loans calculated successfully", savingsService.getTotalLoansOverdue()));
    }

    @GetMapping("/total-loan-interest")
    public ResponseEntity<ApiResponse<TotalInterestCollectedResponse>> getTotalInterest(
            @RequestParam(value = "month", required = false) final Month month,
            @RequestParam(value = "year", required = false) final Year year) {
        return ResponseEntity.ok(ApiResponse.success(true, "Total interest calculated successfully",
                savingsService.getTotalInterestCollected(month, year)));
    }

    @PostMapping("/delete-collateral")
    public ResponseEntity<ApiResponse<String>> deleteCollateral(@RequestParam String loanCollateralId){
        collateralService.deleteCollateral(loanCollateralId);
        return ResponseEntity.ok(ApiResponse.success(true, "Collateral deleted successfully", null));
    }

    @PostMapping("/write-off-loan")
    public ResponseEntity<ApiResponse<String>> writeOffLoan(@RequestParam String loanApplicationId){
        loanApplicationService.writeOff(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan application successfully written-off", null));
    }
}
