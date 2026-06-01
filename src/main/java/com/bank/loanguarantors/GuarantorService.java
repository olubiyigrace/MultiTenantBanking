package com.bank.loanguarantors;

import com.bank.others.utils.PageResponse;

public interface GuarantorService {
    void createGuarantor(GuarantorRequest guarantorRequest);
    void verifyGuarantor(String guarantorMemberId);

    PageResponse<GuarantorResponse> getAllGuarantors(int page, int size);
    void updateGuarantor(String id, GuarantorRequest guarantorRequest);
    void deleteGuarantor(String id);
}
