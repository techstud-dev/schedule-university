package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.entity.PendingRegistration;
import com.techstud.schedule_university.auth.entity.User;
import com.techstud.schedule_university.auth.exception.UserExistsException;

public interface UserCreationService {
    User createPendingUser(PendingRegistration pendingRegistration) throws UserExistsException;
}
