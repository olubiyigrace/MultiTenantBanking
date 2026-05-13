package com.bank.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data@AllArgsConstructor
public class LoginRequest {
    private String adminEmail;
    private String adminPassword;
}
