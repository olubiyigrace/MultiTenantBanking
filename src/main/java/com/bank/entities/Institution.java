package com.bank.entities;

import com.bank.common.AbstractEntity;
import com.bank.enums.InstitutionStatus;
import com.bank.enums.InstitutionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "institutions")
public class Institution extends AbstractEntity {
    private String name;
    private String email;
    private String phone;
    private String rcNumber;
    private String baseCurrency = "NGN";

    @Enumerated(EnumType.STRING)
    private InstitutionType institutionType;

    @Enumerated(EnumType.STRING)
    private InstitutionStatus status;

    @OneToMany(mappedBy = "institution")
    private List<User> user;

    @OneToMany(mappedBy = "institution")
    private List<MemberProfile> memberProfile;

    @OneToMany(mappedBy = "institution")
    private List<SavingsAccount> savingsAccount;

    @OneToMany(mappedBy = "institution")
    private List<Transaction> transaction;

    @OneToMany(mappedBy = "institution")
    private List<LoanProduct> loanProduct;

    @OneToMany(mappedBy = "institution")
    private List<LoanApplication> loanApplication;

    @OneToMany(mappedBy = "institution")
    private List<AuditLog> auditLog;

}
