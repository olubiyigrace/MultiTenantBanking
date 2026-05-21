package com.bank.mapper;

import com.bank.entities.MemberProfile;
import com.bank.requests.MemberRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MemberMapper {
    public MemberProfile toEntity(MemberRequest memberRequest) {
        return MemberProfile.builder()
                .bvn(memberRequest.getBvn())
                .address(memberRequest.getAddress())
                .employmentStatus(memberRequest.getEmploymentStatus())
                .monthlyIncome(memberRequest.getMonthlyIncome())
                .nextOfKinName(memberRequest.getNextOfKinName())
                .nextOfKinPhone(memberRequest.getNextOfKinPhone())
                .dateOfBirth(memberRequest.getDateOfBirth())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
