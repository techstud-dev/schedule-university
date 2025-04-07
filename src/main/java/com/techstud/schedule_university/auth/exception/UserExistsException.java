package com.techstud.schedule_university.auth.exception;

import lombok.experimental.StandardException;

@StandardException
public class UserExistsException extends Exception {

    private static final String STANDARD_MESSAGE = "User with these credentials already exists";

    public UserExistsException() {
        super(STANDARD_MESSAGE);
    }
}
