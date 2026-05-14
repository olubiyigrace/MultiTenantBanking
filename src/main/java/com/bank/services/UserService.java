package com.bank.services;

import com.bank.common.PageResponse;
import com.bank.requests.RegisterUserRequest;
import com.bank.entities.User;
import com.bank.responses.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void createUser(final RegisterUserRequest registerUserRequest);
    void updateUser(final String id, final RegisterUserRequest registerUserRequest);
    UserResponse getSingleUser(final String id);
    PageResponse<UserResponse> getAllUsers(final int page, final int size);
    void deleteUser(final String id);
    void enableUser(final String userId);
    void disableUser(final String userId);
}
