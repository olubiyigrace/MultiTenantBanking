package com.bank.controllers;

import com.bank.loanrepaymentschedule.OverdueRepaymentScheduleResponse;
import com.bank.memberprofiles.MemberRequest;
import com.bank.loanapplications.LoanApplicationResponse;
import com.bank.others.utils.PageResponse;
import com.bank.loanapplications.LoanApplicationService;
import com.bank.memberprofiles.MemberService;
import com.bank.others.utils.ApiResponse;
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

    @PostMapping("/register-members")
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
        return ResponseEntity.ok(ApiResponse.success(true, "Loan application approval recommended",
                null));
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
