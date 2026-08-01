package com.bank.controllers;

import com.bank.dtos.responseDtos.OverdueRepaymentScheduleResponse;
import com.bank.dtos.requestDtos.MemberRequest;
import com.bank.dtos.responseDtos.LoanApplicationResponse;
import com.bank.utils.PageResponse;
import com.bank.services.LoanApplicationService;
import com.bank.services.MemberService;
import com.bank.utils.ApiResponse;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('LOAN_OFFICER')")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoanOfficerController {
    private final MemberService memberService;
    private final LoanApplicationService loanApplicationService;

    @PostMapping("/register-members") // working
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody MemberRequest memberRequest) throws MessagingException {
        memberService.createMember(memberRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Member registered successfully!", null));
    }

    @GetMapping("/all-assigned-applications")
    public ResponseEntity<ApiResponse<PageResponse<LoanApplicationResponse>>> getAssignedApplications(
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "10") final int size) {
        return ResponseEntity.ok(ApiResponse.success(true, "Assigned loan applications retrieved successfully",
                loanApplicationService.getAllAssignedApplications(page, size)));
    }

    @PostMapping("/recommend-approval")
    public ResponseEntity<ApiResponse<String>> recommendApproval(@RequestParam String loanApplicationId) {
        loanApplicationService.recommendApproval(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan application approval recommended", null));
    }

    @PostMapping("/recommend-rejection")
    public ResponseEntity<ApiResponse<String>> recommendRejection(@RequestParam String loanApplicationId) {
        loanApplicationService.recommendRejection(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan application approval recommended",
                null));
    }

    @GetMapping("/all-overdue-repayment")
    public ResponseEntity<ApiResponse<PageResponse<OverdueRepaymentScheduleResponse>>> getOverdueRepayments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(true, "Overdue payments retrieved successfully",
                loanApplicationService.getOverdueRepaymentSchedules(page, size)));
    }
}
