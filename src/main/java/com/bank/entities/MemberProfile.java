package com.bank.entities;

import com.bank.common.AbstractEntity;
import com.bank.enums.ProfileStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "member_profiles")
public class MemberProfile extends AbstractEntity {
    private String bvn;
    private String address;
    private String employmentStatus;
    private Double monthlyIncome;
    private String nextOfKinName;
    private String nextOfKinPhone;
    private LocalDate dateOfBirth;

    @Column(unique = true)
    private String memberNumber;

    @Enumerated(EnumType.STRING)
    private ProfileStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "memberProfile")
    private List<SavingsAccount> savingsAccount;

    @OneToOne(mappedBy = "memberProfile")
    private LoanApplication loanApplication;
}
