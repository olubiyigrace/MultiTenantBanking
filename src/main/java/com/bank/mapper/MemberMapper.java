package com.bank.mapper;

import com.bank.auth.response.UserResponse;
import com.bank.entities.MemberProfile;
import com.bank.entities.SavingsAccount;
import com.bank.entities.User;
import com.bank.enums.ProfileStatus;
import com.bank.enums.SavingsAccountType;
import com.bank.enums.UserAccountType;
import com.bank.requests.MemberRequest;
import com.bank.responses.MemberResponse;
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
                .profileStatus(ProfileStatus.ACTIVE)
                .user(User.builder()
                        .name(memberRequest.getRegisterUserRequest().getName())
                        .username(memberRequest.getRegisterUserRequest().getEmail())
                        .password(passwordEncoder.encode(memberRequest.getRegisterUserRequest().getPassword()))
                        .phone(memberRequest.getRegisterUserRequest().getPhone())
                        .nin(memberRequest.getRegisterUserRequest().getNin())
                        .isVerified(false)
                        .email(memberRequest.getRegisterUserRequest().getEmail())
                        .userAccountType(UserAccountType.MEMBER)
                        .build())
                .savingsAccount(SavingsAccount.builder()
                        .balance(memberRequest.getSavingsAccountRequest().getBalance())
                        .targetAmount(memberRequest.getSavingsAccountRequest().getTargetAmount())
                        .maturityDate(memberRequest.getSavingsAccountRequest().getMaturityDate())
                        .savingsAccountType(SavingsAccountType.REGULAR)
                        .build())
                .build();
    }

    public MemberResponse toResponse(MemberProfile memberProfile){
        return MemberResponse.builder()
                .id(memberProfile.getId())
                .profileStatus(memberProfile.getProfileStatus())
                .bvn(memberProfile.getBvn())
                .address(memberProfile.getAddress())
                .employmentStatus(memberProfile.getEmploymentStatus())
                .monthlyIncome(memberProfile.getMonthlyIncome())
                .nextOfKinName(memberProfile.getNextOfKinName())
                .nextOfKinPhone(memberProfile.getNextOfKinPhone())
                .dateOfBirth(memberProfile.getDateOfBirth())
                .userResponse(UserResponse.builder()
                        .id(memberProfile.getUser().getId())
                        .name(memberProfile.getUser().getName())
                        .email(memberProfile.getUser().getEmail())
                        .phone(memberProfile.getUser().getPhone())
                        .nin(memberProfile.getUser().getNin())
                        .userAccountType(UserAccountType.MEMBER)
                        .build())
                .build();
    }
}
