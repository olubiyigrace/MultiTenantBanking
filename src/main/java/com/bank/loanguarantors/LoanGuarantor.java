package com.bank.loanguarantors;


import com.bank.others.AbstractEntity;
import com.bank.loanapplications.LoanApplication;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "loan_guarantors")
public class LoanGuarantor extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", foreignKey = @ForeignKey(name = "fk_loan_guarantor_loan_application_id"))
    private LoanApplication loanApplication;

    private String guarantorMemberId;

    @Enumerated(EnumType.STRING)
    private GuarantorStatus guarantorStatus;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime respondedAt;
}
