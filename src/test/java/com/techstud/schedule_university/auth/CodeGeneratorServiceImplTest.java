package com.techstud.schedule_university.auth;


import com.techstud.schedule_university.auth.service.CodeGeneratorService;
import com.techstud.schedule_university.auth.service.impl.CodeGeneratorServiceImpl;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CodeGeneratorServiceImplTest {

    private final CodeGeneratorService service = new CodeGeneratorServiceImpl();

    @RepeatedTest(10)
    void generateCode_ShouldReturnValidFormat() {
        // Act
        String code = service.generateCode();

        // Assert
        assertTrue(code.matches("\\d{6}"), "Code should be 6 digits");
        int numericCode = Integer.parseInt(code);
        assertTrue(numericCode >= 0 && numericCode <= 999999);
    }

    @Test
    void generateCode_CheckFullRange() {
        // Stress test
        for (int i = 0; i < 10000; i++) {
            String code = service.generateCode();
            assertEquals(6, code.length());
            assertDoesNotThrow(() -> Integer.parseInt(code));
        }
    }
}
