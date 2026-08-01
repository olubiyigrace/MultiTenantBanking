package com.bank.entities;

import com.bank.enums.ProfileStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "member_profiles")
public class MemberProfile extends AbstractEntity {
    @Column(nullable = false)
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

    @Column(nullable = false)
    private String memberNumber;

    @Enumerated(EnumType.STRING)
    private ProfileStatus profileStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_member_profile_user_id"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_member_profile_institution_id"))
    private Institution institution;
}
