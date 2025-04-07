package com.techstud.schedule_university.auth.exception;

import lombok.experimental.StandardException;

@StandardException
public class InvalidJwtTokenException extends Exception {

    private static final String STANDARD_MESSAGE = "Token expired or incorrect";

    public InvalidJwtTokenException() {
        super(STANDARD_MESSAGE);
    }
}
