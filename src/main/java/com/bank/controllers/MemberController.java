package com.bank.controllers;

import com.bank.common.PageResponse;
import com.bank.enums.ProfileStatus;
import com.bank.requests.MemberRequest;
import com.bank.responses.MemberResponse;
import com.bank.services.MemberService;
import com.bank.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/members")
public class MemberController {
    private final MemberService memberService;

    @PreAuthorize("hasRole('LOAN_OFFICER')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody MemberRequest memberRequest){
        memberService.createMember(memberRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Member registered successfully!", null));
    }

    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<PageResponse<MemberResponse>>> getMembers(
            @RequestParam ProfileStatus profileStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10")  int size) {
        return ResponseEntity.ok(ApiResponse.success(true, "Members retrieved successfully",
                memberService.getAllMembers(profileStatus, page, size)));
    }
}

