package com.techstud.schedule_university.auth.exception;

import lombok.experimental.StandardException;

@StandardException
public class InvalidCodeException extends Exception {
    private static final String MESSAGE = "Confirmation code is invalid!";

    public InvalidCodeException() {
        super(MESSAGE);
    }
}
