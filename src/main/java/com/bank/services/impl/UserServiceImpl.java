package com.bank.services.impl;

import com.bank.requests.RegisterUserRequest;
import com.bank.entities.User;
import com.bank.enums.UserAccountType;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.UnauthorizedException;
import com.bank.mapper.UserMapper;
import com.bank.repositories.UserRepository;
import com.bank.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public void registerUser(RegisterUserRequest registerUserRequest, User loggedInUser) {
        if(loggedInUser.getUserAccountType() != UserAccountType.INSTITUTION_ADMIN){
            throw new UnauthorizedException("Only institution admins can create users.");
        }
        if(userRepository.existsByEmail(registerUserRequest.getEmail())){
            throw new DuplicateResourceException("User with the email already exists.");
        }
        if(registerUserRequest.getUserAccountType() != UserAccountType.ACCOUNTANT
        && registerUserRequest.getUserAccountType() != UserAccountType.LOAN_OFFICER
        && registerUserRequest.getUserAccountType() != UserAccountType.MEMBER){
            throw new UnauthorizedException("Invalid account type");
        }
        User newUser = userMapper.toEntity(registerUserRequest, loggedInUser);
        userRepository.save(newUser);
    }
}
