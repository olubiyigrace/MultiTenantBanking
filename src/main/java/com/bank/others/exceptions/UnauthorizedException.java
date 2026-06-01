package com.bank.others.exceptions;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
