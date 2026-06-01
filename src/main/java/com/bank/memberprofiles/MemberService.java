package com.bank.memberprofiles;


import com.bank.others.utils.PageResponse;
import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface MemberService extends UserDetailsService {
    void createMember(MemberRequest memberRequest) throws MessagingException;
    PageResponse<MemberResponse> getAllMembers(final ProfileStatus profileStatus, final int page, final int size);
}
