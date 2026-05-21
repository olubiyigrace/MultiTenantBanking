package com.bank.controllers;

import com.bank.auth.requests.RefreshTokenRequest;
import com.bank.auth.response.LoginResponse;
import com.bank.common.PageResponse;
import com.bank.entities.Institution;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.responses.InstitutionResponse;
import com.bank.responses.TotalMemberResponse;
import com.bank.responses.TotalMembersStatisticsResponse;
import com.bank.services.InstitutionService;
import com.bank.utils.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
@Tag(name = "Institution", description = "Institution API")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class InstitutionController {
    private final InstitutionService institutionService;

    @PostMapping("/approve/{institution-id}")
    public ResponseEntity<ApiResponse<String>> approveInstitution(@PathVariable("institution-id") final String institutionId) {
        institutionService.approveInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Institution approved!", null));
    }

    @PatchMapping("/activate/{institution-id}")
    public ResponseEntity<ApiResponse<String>> activateInstitution(@PathVariable("institution-id") final String institutionId) {
        institutionService.activateInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Institution activated successfully!", null));
    }

    @PatchMapping("/deactivate/{institution-id}")
    public ResponseEntity<ApiResponse<String>> deactivateInstitution(@PathVariable("institution-id") final String  institutionId) {
        institutionService.deactivateInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Institution deactivated successfully!", null));
    }

    @PatchMapping("/suspend/{institution-id}")
    public ResponseEntity<ApiResponse<String>> suspendInstitution(@PathVariable("institution-id") final String  institutionId) {
        institutionService.suspendInstitution(institutionId);
        return ResponseEntity.ok(ApiResponse.success(true, "Institution suspended successfully!", null));
    }

    @GetMapping
    public ResponseEntity<PageResponse<InstitutionResponse>> getAllInstitution(
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "10") final int size) {
        return ResponseEntity.ok(institutionService.findAllInstitution(page, size));
    }

    @GetMapping("/members-count")
    public ResponseEntity<TotalMembersStatisticsResponse> getMembersStatistics() {
        return ResponseEntity.ok(institutionService.getMembersStatistics());
    }
//
//    @GetMapping("/deposits")
//    public ResponseEntity<BigDecimal> getInstitutionTotalDeposits() {
//        return ResponseEntity.ok(institutionService.getTotalDepositsAcrossInstitutions());
//    }

//    @GetMapping("/deposits")
//    @GetMapping("/loan-outstanding")
//    @GetMapping("/savings-balance")
//    @GetMapping("/loan-disbursed-per-month")
}