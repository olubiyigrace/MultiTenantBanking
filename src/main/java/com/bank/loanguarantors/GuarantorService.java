package com.bank.loanguarantors;

import com.bank.others.utils.PageResponse;
import jakarta.mail.MessagingException;

public interface GuarantorService {
    void createGuarantor(GuarantorRequest guarantorRequest) throws MessagingException;
    void acceptGuarantorRequest(String loanApplicationId);
    void rejectGuarantorRequest(String loanApplicationId);
    PageResponse<GuarantorResponse> getAllGuarantors(int page, int size);
}
