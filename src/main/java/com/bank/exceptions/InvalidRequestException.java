package com.bank.exceptions;

public class InvalidRequestException extends BusinessException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
