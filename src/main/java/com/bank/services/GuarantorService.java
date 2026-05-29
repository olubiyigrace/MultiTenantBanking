package com.bank.services;

import com.bank.requests.GuarantorRequest;
import com.bank.responses.GuarantorResponse;
import com.bank.responses.PageResponse;

public interface GuarantorService {
    void createGuarantor(GuarantorRequest guarantorRequest);
    PageResponse<GuarantorResponse> getAllGuarantors(int page, int size);
    void updateGuarantor(String id, GuarantorRequest guarantorRequest);
    void deleteGuarantor(String id);
}
