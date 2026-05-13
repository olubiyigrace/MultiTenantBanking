//package com.bank.entities;
//
//
//import com.bank.common.AbstractEntity;
//import com.bank.enums.SavingsAccountType;
//import com.bank.enums.SavingsStatus;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.experimental.SuperBuilder;
//
//import java.util.List;
//
//@Entity
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@SuperBuilder
//@Table(name = "savings_accounts")
//public class SavingsAccount extends AbstractEntity {
//    private String balance;
//    private String minimumBalance;
//    private String interestRatePercent;
//    private String targetAmount;
//    private String maturityDate;
//
//    @Column(unique = true)
//    private String accountNumber;
//
//    @Enumerated(EnumType.STRING)
//    private SavingsStatus savingsStatus;
//
//    @Enumerated(EnumType.STRING)
//    private SavingsAccountType savingsAccountType;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    private Institution institution;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    private MemberProfile memberProfile;
//
//    @OneToMany(mappedBy = "savingsAccount")
//    private List<Transaction> transaction;
//
//    @OneToOne(mappedBy = "savingsAccount")
//    private SavingsInterestAccrual savingsInterestAccrual;
//}
