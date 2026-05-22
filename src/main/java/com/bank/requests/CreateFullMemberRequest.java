package com.bank.requests;

import com.bank.auth.requests.RegisterUserRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateFullMemberRequest {
    private MemberRequest memberRequest;
    private RegisterUserRequest registerUserRequest;
}
