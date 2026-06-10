package com.bank.others.login;

import com.bank.others.auth.UserInstitutionsResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class SelectInstitutionResponse {
    private String loginType;
    private String loginToken;
    private List<UserInstitutionsResponse> institutions;
}
