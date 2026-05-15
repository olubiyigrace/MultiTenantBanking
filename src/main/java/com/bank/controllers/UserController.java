package com.bank.controllers;

import com.bank.requests.RegisterUserRequest;
import com.bank.entities.User;
import com.bank.services.UserService;
import com.bank.utils.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@Tag(name = "User", description = "User API")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody final RegisterUserRequest registerUserRequest){
        userService.createUser(registerUserRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "User registered successfully!", null));
    }

//        @GetMapping
//        @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMINISTRATOR')")
//        public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
//                @RequestParam(name = "page", defaultValue = "0")
//                final int page,
//                @RequestParam(name = "size", defaultValue = "10")
//                final int size
//        ) {
//            final PageResponse<UserResponse> response = this.userService.getAllUsers(page, size);
//            return ResponseEntity.ok(response);
//        }
//
//        @GetMapping("/{user-id}")
//        @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMINISTRATOR')")
//        public ResponseEntity<UserResponse> getUserById(
//                @PathVariable("user-id")
//                final String id) {
//            final UserResponse response = this.userService.getUserById(id);
//            return ResponseEntity.ok(response);
//        }
//
//        @PutMapping("/{user-id}")
//        @PreAuthorize("hasRole('COMPANY_ADMIN')")
//        public ResponseEntity<Void> updateUser(
//                @PathVariable("user-id")
//                final String id,
//                @Valid
//                @RequestBody
//                final UserRequest request) {
//            this.userService.updateUser(id, request);
//            return ResponseEntity.status(HttpStatus.ACCEPTED)
//                    .build();
//        }
//
//        @DeleteMapping("/{user-id}")
//        @PreAuthorize("hasRole('COMPANY_ADMIN')")
//        public ResponseEntity<Void> deleteUser(
//                @PathVariable("user-id")
//                final String id) {
//            this.userService.deleteUser(id);
//            return ResponseEntity.noContent()
//                    .build();
//        }
//
//        @PutMapping("/{user-id}/enable")
//        @PreAuthorize("hasRole('COMPANY_ADMIN')")
//        public ResponseEntity<Void> enableUser(
//                @PathVariable("user-id")
//                final String id) {
//            this.userService.enableUser(id);
//            return ResponseEntity.status(HttpStatus.ACCEPTED)
//                    .build();
//        }
//
//        @PutMapping("/{user-id}/disable")
//        @PreAuthorize("hasRole('COMPANY_ADMIN')")
//        public ResponseEntity<Void> disableUser(
//                @PathVariable("user-id")
//                final String id) {
//            this.userService.disableUser(id);
//            return ResponseEntity.status(HttpStatus.ACCEPTED)
//                    .build();
//        }
    }
