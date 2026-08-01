package com.bank.dtos.responseDtos;

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
