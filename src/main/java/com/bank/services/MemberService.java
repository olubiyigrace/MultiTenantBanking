package com.bank.services;


import com.bank.common.PageResponse;
import com.bank.enums.ProfileStatus;
import com.bank.requests.MemberRequest;
import com.bank.responses.MemberResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface MemberService extends UserDetailsService {
    void createMember(MemberRequest memberRequest);
    PageResponse<MemberResponse> getAllMembers(final ProfileStatus profileStatus, final int page, final int size);




//    void updateUser(final String id, final RegisterUserRequest registerUserRequest);
//    UserResponse getSingleUser(final String id);
//    PageResponse<UserResponse> getAllUsers(final int page, final int size);
//    void deleteUser(final String id);
//    void enableUser(final String userId);
//    void disableUser(final String userId);
}
