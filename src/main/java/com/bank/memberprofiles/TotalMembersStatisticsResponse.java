package com.bank.memberprofiles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class TotalMembersStatisticsResponse {
    private Long totalInstitutionMembers;
    private List<TotalMemberResponse> institutions;
}