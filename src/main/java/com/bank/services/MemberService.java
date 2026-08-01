package com.bank.services;


import com.bank.dtos.requestDtos.MemberRequest;
import com.bank.dtos.responseDtos.MemberResponse;
import com.bank.enums.ProfileStatus;
import com.bank.utils.PageResponse;
import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface MemberService extends UserDetailsService {
    void createMember(MemberRequest memberRequest) throws MessagingException;
    PageResponse<MemberResponse> getAllMembers(final ProfileStatus profileStatus, final int page, final int size);
}
