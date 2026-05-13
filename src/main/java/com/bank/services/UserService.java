package com.bank.services;

import com.bank.requests.RegisterUserRequest;
import com.bank.entities.User;

public interface UserService {
    void registerUser(RegisterUserRequest registerUserRequest, User loggedInUser);
}
