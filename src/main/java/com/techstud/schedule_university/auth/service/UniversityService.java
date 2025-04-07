package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.entity.University;

public interface UniversityService {
    University resolveUniversity(String name);
}
