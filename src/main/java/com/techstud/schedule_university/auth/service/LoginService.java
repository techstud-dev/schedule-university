package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.dto.request.LoginDTO;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationDTO;

public interface LoginService {
    SuccessAuthenticationDTO processLogin(LoginDTO dto) throws Exception;
}
