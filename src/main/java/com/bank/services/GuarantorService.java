package com.bank.services;

import com.bank.dtos.requestDtos.GuarantorRequest;
import jakarta.mail.MessagingException;

public interface GuarantorService {
    void createGuarantor(GuarantorRequest guarantorRequest) throws MessagingException;
    void acceptGuarantorRequest(String loanApplicationId);
    void rejectGuarantorRequest(String loanApplicationId);
}
