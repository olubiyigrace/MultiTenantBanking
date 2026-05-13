//package com.bank.entities;
//
//import com.bank.common.AbstractEntity;
//import com.bank.enums.TransactionStatus;
//import com.bank.enums.TransactionType;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.experimental.SuperBuilder;
//
//@Entity
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@SuperBuilder
//@Table(name = "transactions")
//public class Transaction extends AbstractEntity {
//
//    @Enumerated(EnumType.STRING)
//    private TransactionType transactionType;
//
//    @Enumerated(EnumType.STRING)
//    private TransactionStatus transactionStatus;
//
//    @Column(unique = true)
//    private String reference;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "performed_by_user_id")
//    private User user;
//
//    private String reversedByTransactionId;
//    private String amount;
//    private String balanceBefore;
//    private String balanceAfter;
//    private String description;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    private Institution institution;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    private SavingsAccount savingsAccount;
//}