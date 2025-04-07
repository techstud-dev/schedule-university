package com.techstud.schedule_university.auth.exception;

import lombok.experimental.StandardException;

@StandardException
public class BadCredentialsException extends Exception {

    private static final String STANDARD_MESSAGE = "Incorrect credentials";

    public BadCredentialsException() {
        super(STANDARD_MESSAGE);
    }
}
