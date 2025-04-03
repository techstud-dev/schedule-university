package com.techstud.schedule_university.auth.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtil {

    public <T> ResponseEntity<T> okWithCookies(T body, ResponseCookie... cookies) {
        var response = ResponseEntity.ok();
        for (ResponseCookie cookie : cookies) {
            response = response.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return response.body(body);
    }
}
