package com.techstud.schedule_university.auth.service.impl;

import com.techstud.schedule_university.auth.service.CodeGeneratorService;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Сервис генерации кодов подтверждения
 *
 * <p>Генерирует 6-значные числовые коды</p>
 */
@Service
public class CodeGeneratorServiceImpl implements CodeGeneratorService {

    /**
     * Генерирует случайный 6-значный код
     *
     * @return Код в формате 000000-999999
     */
    @Override
    public String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(100000));
    }
}
