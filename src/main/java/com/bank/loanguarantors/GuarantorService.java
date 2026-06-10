package com.bank.loanguarantors;

import jakarta.mail.MessagingException;

public interface GuarantorService {
    void createGuarantor(GuarantorRequest guarantorRequest) throws MessagingException;
    void acceptGuarantorRequest(String loanApplicationId);
    void rejectGuarantorRequest(String loanApplicationId);
}
