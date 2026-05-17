package com.bank.mapper;

import com.bank.requests.RegisterUserRequest;
import com.bank.entities.User;
import com.bank.responses.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User toEntity(RegisterUserRequest registerUserRequest){
        return User.builder()
                .name(registerUserRequest.getName())
                .password(passwordEncoder.encode(registerUserRequest.getPassword()))
                .phone(registerUserRequest.getPhone())
                .nin(registerUserRequest.getNin())
                .email(registerUserRequest.getEmail())
                .username(registerUserRequest.getEmail())
                .userAccountType(registerUserRequest.getUserAccountType())
                .isVerified(false)
                .build();
    }
    public UserResponse toResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nin(user.getNin())
                .userAccountType(user.getUserAccountType())
                .build();
    }
}
