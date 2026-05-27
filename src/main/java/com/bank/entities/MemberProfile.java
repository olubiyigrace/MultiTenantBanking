package com.bank.entities;

import com.bank.common.AbstractEntity;
import com.bank.enums.ProfileStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
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
    @Column(nullable = false, unique = true)
    private String bvn;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String employmentStatus;

    @Column(nullable = false)
    private BigDecimal monthlyIncome;

    @Column(nullable = false)
    private String nextOfKinName;

    @Column(nullable = false)
    private String nextOfKinPhone;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, unique = true)
    private String memberNumber;

    @Enumerated(EnumType.STRING)
    private ProfileStatus profileStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_member_profile_user_id"))
    private User user;

    @OneToMany(mappedBy = "member")
    private List<SavingsAccount> savingsAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_member_profile_institution_id"))
    private Institution institution;
}
