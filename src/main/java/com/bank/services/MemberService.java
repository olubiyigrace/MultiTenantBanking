package com.bank.services;


import com.bank.auth.requests.RegisterUserRequest;
import com.bank.requests.CreateFullMemberRequest;
import com.bank.requests.MemberRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface MemberService extends UserDetailsService {
    void createMember(CreateFullMemberRequest createFullMemberRequest) ;



//    void updateUser(final String id, final RegisterUserRequest registerUserRequest);
//    UserResponse getSingleUser(final String id);
//    PageResponse<UserResponse> getAllUsers(final int page, final int size);
//    void deleteUser(final String id);
//    void enableUser(final String userId);
//    void disableUser(final String userId);
}
