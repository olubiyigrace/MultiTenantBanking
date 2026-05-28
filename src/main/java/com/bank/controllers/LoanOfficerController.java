package com.bank.controllers;

import com.bank.requests.MemberRequest;
import com.bank.responses.LoanApplicationResponse;
import com.bank.responses.PageResponse;
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
}
