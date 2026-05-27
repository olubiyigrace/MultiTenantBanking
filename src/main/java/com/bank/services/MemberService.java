package com.bank.services;


import com.bank.responses.PageResponse;
import com.bank.enums.ProfileStatus;
import com.bank.requests.MemberRequest;
import com.bank.responses.MemberResponse;
import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface MemberService extends UserDetailsService {
    void createMember(MemberRequest memberRequest) throws MessagingException;
    PageResponse<MemberResponse> getAllMembers(final ProfileStatus profileStatus, final int page, final int size);
}
