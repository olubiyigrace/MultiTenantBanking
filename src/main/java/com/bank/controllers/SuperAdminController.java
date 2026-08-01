package com.bank.controllers;

import com.bank.dtos.responseDtos.TotalLoansDisbursedStatisticsResponse;
import com.bank.dtos.responseDtos.TotalLoansOutstandingStatisticsResponse;
import com.bank.dtos.responseDtos.TotalMembersStatisticsResponse;
import com.bank.dtos.responseDtos.InstitutionResponse;
import com.bank.utils.PageResponse;
import com.bank.services.InstitutionService;
import com.bank.utils.ApiResponse;
import com.bank.dtos.responseDtos.TotalSavingsStatisticsResponse;
import com.bank.dtos.responseDtos.TotalDepositsStatisticsResponse;
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

    @PostMapping("/approve") // working
    public ResponseEntity<ApiResponse<String>> approveInstitution(@RequestParam String institutionId) throws MessagingException {
        institutionService.approveInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Institution approved!", null));
    }

    @PatchMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activateInstitution(@RequestParam String institutionId) {
        institutionService.activateInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Institution activated successfully!", null));
    }

    @PatchMapping("/suspend")
    public ResponseEntity<ApiResponse<String>> suspendInstitution(@RequestParam String  institutionId) {
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
