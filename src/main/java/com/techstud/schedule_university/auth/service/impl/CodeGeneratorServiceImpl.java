package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.service.CodeGeneratorService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CodeGeneratorServiceImpl implements CodeGeneratorService {

    @Override
    public String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(100000));
    }
}
