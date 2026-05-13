package com.bank.responses;

import com.bank.enums.UserAccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserResponse {
    private String name;
    private String email;
    private String phone;
    private String nin;
    private UserAccountType userAccountType;
}
