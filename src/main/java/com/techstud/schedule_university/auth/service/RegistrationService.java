package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.dto.request.RegisterDTO;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationDTO;

public interface RegistrationService {
    SuccessAuthenticationDTO processRegister(RegisterDTO dto) throws Exception;
}
