package com.bank.controllers;

import com.bank.common.PageResponse;
import com.bank.enums.ProfileStatus;
import com.bank.requests.MemberRequest;
import com.bank.responses.LoanProductResponse;
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












//    @GetMapping("/get-all")
//    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
//            @RequestParam(name = "page", defaultValue = "0") final int page,
//            @RequestParam(name = "size", defaultValue = "10") final int size) {
//        return ResponseEntity.ok(memberService.getAllUsers(page, size));
//    }
//
//    @GetMapping("get/{user-id}")
//    public ResponseEntity<UserResponse> getUserById(@PathVariable("user-id") final String id) {
//        final UserResponse response = memberService.getSingleUser(id);
//        return ResponseEntity.ok(response);
//    }
//
//    @PutMapping("update/{user-id}")
//    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable("user-id") final String id, @Valid @RequestBody final RegisterUserRequest request) {
//        memberService.updateUser(id, request);
//        return ResponseEntity.ok(ApiResponse.success(true, "User updated successfully", null));
//    }
//
//    @DeleteMapping("delete/{user-id}")
//    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable("user-id") final String id) {
//        memberService.deleteUser(id);
//        return ResponseEntity.ok(ApiResponse.success(true, "User deleted successfully", null));
//    }
//
//    @PutMapping("enable/{user-id}")
//    public ResponseEntity<ApiResponse<String>> enableUser(@PathVariable("user-id") final String id) {
//        memberService.enableUser(id);
//        return ResponseEntity.ok(ApiResponse.success(true, "User enabled successfully", null));
//    }
//
//    @PutMapping("disable/{user-id}")
//    public ResponseEntity<ApiResponse<String>> disableUser(@PathVariable("user-id") final String id) {
//        memberService.disableUser(id);
//        return ResponseEntity.ok(ApiResponse.success(true, "User disabled successfully", null));
//    }

