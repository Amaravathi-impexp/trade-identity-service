package com.amaravathi.tradeidentity.common;

public class TradeIdentityException extends RuntimeException {
    public TradeIdentityException(String message) {
        super(message);
    }

    public TradeIdentityException(String message, Exception e) {
        super(message);
    }
}
