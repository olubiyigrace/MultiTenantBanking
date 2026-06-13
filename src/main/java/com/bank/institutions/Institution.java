package com.bank.institutions;

import com.bank.memberprofiles.MemberProfile;
import com.bank.others.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "institutions")
public class Institution extends AbstractEntity {

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false)
    private String institutionPhone;

    @Column(unique = true, nullable = false)
    private String institutionRcNumber;

    @Column(nullable = false, unique = true)
    private String institutionEmail;

    @Column(unique = true, nullable = false, length = 4)
    private String institutionCode;

    @Column(nullable = false)
    @Builder.Default
    private Long nextMemberSequence = 0L;

    @Builder.Default
    private String baseCurrency = "NGN";

    @Enumerated(EnumType.STRING)
    private InstitutionType institutionType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private InstitutionStatus institutionStatus = InstitutionStatus.PENDING;

    @Column(nullable = false)
    private String adminName;

    @Column(nullable = false)
    private String adminUsername;

    @Column(nullable = false)
    private String adminEmail;

    @Column(nullable = false)
    private String adminPhone;

    @Column(nullable = false)
    private String adminNin;

    @Column(nullable = false)
    private String adminPassword;

    @Builder.Default
    private Boolean isVerified = false;

    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;
    private LocalDateTime emailVerifiedAt;

    @Builder.Default
    @OneToMany(mappedBy = "institution")
    private List<MemberProfile> memberProfiles = new ArrayList<>();
}
