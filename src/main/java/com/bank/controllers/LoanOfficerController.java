package com.bank.controllers;

import com.bank.requests.MemberRequest;
import com.bank.services.MemberService;
import com.bank.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasRole('LOAN_OFFICER')")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoanOfficerController {
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody MemberRequest memberRequest){
        memberService.createMember(memberRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Member registered successfully!", null));
    }
}
