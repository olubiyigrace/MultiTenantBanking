package com.bank.mapper;

import com.bank.requests.RegisterUserRequest;
import com.bank.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User toEntity(RegisterUserRequest registerUserRequest, User loggedinUser){
        return User.builder()
                .name(registerUserRequest.getName())
                .password(passwordEncoder.encode(registerUserRequest.getPassword()))
                .phone(registerUserRequest.getPhone())
                .nin(registerUserRequest.getNin())
                .email(registerUserRequest.getEmail())
                .userAccountType(registerUserRequest.getUserAccountType())
                .institution(loggedinUser.getInstitution())
                .build();
    }
}
