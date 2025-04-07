package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.dto.request.LoginRecord;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationRecord;

public interface LoginService {
    SuccessAuthenticationRecord processLogin(LoginRecord dto) throws Exception;
}
