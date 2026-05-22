package com.bank.controllers;

import com.bank.services.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
@PreAuthorize("hasRole('INSTITUTION_ADMIN')")
public class LoanProductController {
    private final LoanProductService loanProductService;
}
