package com.bank.controllers;

import com.bank.loanapplications.TotalLoansDisbursedStatisticsResponse;
import com.bank.loanrepaymentschedule.TotalLoansOutstandingStatisticsResponse;
import com.bank.memberprofiles.TotalMembersStatisticsResponse;
import com.bank.institutions.InstitutionResponse;
import com.bank.others.utils.PageResponse;
import com.bank.institutions.InstitutionService;
import com.bank.others.utils.ApiResponse;
import com.bank.savingsaccount.TotalSavingsStatisticsResponse;
import com.bank.transactions.TotalDepositsStatisticsResponse;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.time.Year;

@PreAuthorize("hasRole('SUPER_ADMIN')")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SuperAdminController {
    private final InstitutionService institutionService;

    @PostMapping("/approve/{institution-id}")
    public ResponseEntity<ApiResponse<String>> approveInstitution(@PathVariable("institution-id") final String institutionId) throws MessagingException {
        institutionService.approveInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Institution approved!", null));
    }

    @PatchMapping("/activate/{institution-id}")
    public ResponseEntity<ApiResponse<String>> activateInstitution(@PathVariable("institution-id") final String institutionId) {
        institutionService.activateInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Institution activated successfully!", null));
    }

    @PatchMapping("/suspend/{institution-id}")
    public ResponseEntity<ApiResponse<String>> suspendInstitution(@PathVariable("institution-id") final String  institutionId) {
        institutionService.suspendInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Institution suspended successfully!", null));
    }

    @GetMapping("/all-institutions")
    public ResponseEntity<ApiResponse<PageResponse<InstitutionResponse>>> getAllInstitution(
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "10") final int size) {
        return ResponseEntity.ok(ApiResponse.success(true, "Institutions retrieved successfully", institutionService.findAllInstitution(page, size)));
    }

    @GetMapping("/total-members")
    public ResponseEntity<ApiResponse<TotalMembersStatisticsResponse>> getMembersStatistics() {
        return ResponseEntity.ok(ApiResponse.success(true, "Total members retrieved successfully", institutionService.getMembersStatistics()));
    }

    @GetMapping("/savings")
    public ResponseEntity<ApiResponse<TotalSavingsStatisticsResponse>> getSavingsStatistics()  {
        return ResponseEntity.ok(ApiResponse.success(true, "Total savings calculated successfully", institutionService.getSavingsStatistics()));
    }

    @GetMapping("/total-loans-outstanding")
    public ResponseEntity<ApiResponse<TotalLoansOutstandingStatisticsResponse>> getLoansOutstandingStatistics()  {
        return ResponseEntity.ok(ApiResponse.success(true, "Total outstanding loans calculated successfully", institutionService.getLoansOutstandingStatistics()));
    }

    @GetMapping("/total-deposits")
    public ResponseEntity<ApiResponse<TotalDepositsStatisticsResponse>> getDepositsStatistics()  {
        return ResponseEntity.ok(ApiResponse.success(true, "Total deposits calculated successfully", institutionService.getDepositsStatistics()));
    }

    @GetMapping("/total-loan-disbursed")
    public ResponseEntity<ApiResponse<TotalLoansDisbursedStatisticsResponse>> getLoansDisbursedStatistics(
            @RequestParam(value = "month", required = false) final Month month,
            @RequestParam(value = "year", required = false) final Year year) {
        return ResponseEntity.ok(ApiResponse.success(true, "Total loan disbursed in " + month + " " + year + " calculated successfully", institutionService.getLoansDisbursedStatistics(month, year)));
    }
}
