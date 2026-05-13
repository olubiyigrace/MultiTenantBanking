//package com.bank.entities;
//
//import com.bank.common.AbstractEntity;
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
//@Table(name = "loan_collaterals")
//public class LoanCollateral extends AbstractEntity {
//    private String description;
//    private String estimatedValue;
//    private String documentUrl;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    private LoanApplication loanApplication;
//}
