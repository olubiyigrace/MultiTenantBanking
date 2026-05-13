package com.bank.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenPair {
    private String accessToken;
    private String refreshToken;
}
