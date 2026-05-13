//package com.bank.entities;
//
//import com.bank.enums.LoanRepaymentStatus;
//import com.fasterxml.jackson.annotation.JsonFormat;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Entity
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class LoanRepaymentSchedule {
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    private LoanApplication loanApplication;
//
//    private String installmentNumber;
//    private String principalDue;
//    private String interestDue;
//    private String totalDue;
//    private String amountPaid;
//    private String balanceRemaining;
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
//    private LocalDate dueDate;
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
//    private LocalDateTime paidAt;
//
//    @Enumerated(EnumType.STRING)
//    private LoanRepaymentStatus loanRepaymentStatus;
//}
