package com.bank.mapper;

import com.bank.entities.MemberProfile;
import com.bank.entities.User;
import com.bank.enums.SavingsAccountType;
import com.bank.enums.UserAccountType;
import com.bank.requests.MemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MemberMapper {
    private final PasswordEncoder passwordEncoder;

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
                .savingsAccountType(SavingsAccountType.REGULAR)
                .user(User.builder()
                        .name(memberRequest.getRegisterUserRequest().getName())
                        .username(memberRequest.getRegisterUserRequest().getEmail())
                        .password(passwordEncoder.encode(memberRequest.getRegisterUserRequest().getPassword()))
                        .phone(memberRequest.getRegisterUserRequest().getPhone())
                        .nin(memberRequest.getRegisterUserRequest().getNin())
                        .email(memberRequest.getRegisterUserRequest().getEmail())
                        .userAccountType(UserAccountType.MEMBER)
                        .build())
                .build();
    }
}
