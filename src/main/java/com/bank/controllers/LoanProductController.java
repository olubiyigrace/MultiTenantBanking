package com.bank.controllers;

import com.bank.services.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class LoanProductController {
    private final LoanProductService loanProductService;
}
