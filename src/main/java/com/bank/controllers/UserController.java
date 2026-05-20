package com.bank.controllers;

import com.bank.common.PageResponse;
import com.bank.requests.RegisterUserRequest;
import com.bank.responses.UserResponse;
import com.bank.services.UserService;
import com.bank.utils.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@PreAuthorize("hasRole('INSTITUTION_ADMIN')")
@Tag(name = "User", description = "User API")
public class UserController {
    private final UserService userService;

    @GetMapping("/get-all")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") final int page,
            @RequestParam(name = "size", defaultValue = "10") final int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("get/{user-id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("user-id") final String id) {
        final UserResponse response = userService.getSingleUser(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("update/{user-id}")
    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable("user-id") final String id, @Valid @RequestBody final RegisterUserRequest request) {
        userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(true, "User updated successfully", null));
    }

    @DeleteMapping("delete/{user-id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable("user-id") final String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(true, "User deleted successfully", null));

    }

    @PutMapping("enable/{user-id}")
    public ResponseEntity<ApiResponse<String>> enableUser(@PathVariable("user-id") final String id) {
        userService.enableUser(id);
        return ResponseEntity.ok(ApiResponse.success(true, "User enabled successfully", null));
    }

    @PutMapping("disable/{user-id}")
    public ResponseEntity<ApiResponse<String>> disableUser(@PathVariable("user-id") final String id) {
        userService.disableUser(id);
        return ResponseEntity.ok(ApiResponse.success(true, "User disabled successfully", null));
    }
}
