package com.bank.memberprofiles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TotalMemberResponse {
    private String institutionId;
    private String institutionName;
    private Long totalMembers;
}
