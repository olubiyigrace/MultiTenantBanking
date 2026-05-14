package com.bank.controllers;

import com.bank.requests.RegisterUserRequest;
import com.bank.entities.User;
import com.bank.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class UserController {
    private final UserService userService;

    @PostMapping("/register-usser")
    public ResponseEntity<Void> registerUser(@RequestBody final RegisterUserRequest registerUserRequest){
        userService.createUser(registerUserRequest);
        return ResponseEntity.ok().build();
    }
}
