package com.techstud.schedule_university.auth.service;

import com.techstud.schedule_university.auth.entity.Role;

public interface RoleService {
    Role resolveRole(String name);
}
