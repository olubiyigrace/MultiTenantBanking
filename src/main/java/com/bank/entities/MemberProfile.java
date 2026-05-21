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
    private BigDecimal monthlyIncome;
    private String nextOfKinName;
    private String nextOfKinPhone;
    private LocalDate dateOfBirth;

    @Column(unique = true)
    private String memberNumber;

    @Enumerated(EnumType.STRING)
    private ProfileStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_member_profile_user_id"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_member_profile_institution_id"))
    private Institution institution;
}
