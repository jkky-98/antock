package com.antock.task.service.externalapi.regioncode;

public class WrongAddressException extends RuntimeException {
    public WrongAddressException(String message) {
        super(message);
    }
}
