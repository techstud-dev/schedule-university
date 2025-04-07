package com.techstud.schedule_university.auth.swagger;

public class AuthApiExamples {

    // Login examples
    public static final String LOGIN_EXAMPLE = """
        {
           "data": {
              "identificationField": "user@example.com",
              "password": "SecurePass123!"
           }
        }
        """;

    public static final String LOGIN200_RESPONSE = """
        {
            "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        }
        """;

    public static final String LOGIN400_RESPONSE = """
        {
            "systemName": "auth-service",
            "applicationName": "university-auth",
            "error": "Validation failed"
        }
        """;

    public static final String LOGIN401_RESPONSE = """
        {
            "systemName": "auth-service",
            "applicationName": "university-auth",
            "error": "Invalid credentials"
        }
        """;

    public static final String LOGIN404_RESPONSE = """
        {
            "systemName": "auth-service",
            "applicationName": "university-auth",
            "error": "User not found"
        }
        """;

    // Register examples
    public static final String REGISTER_EXAMPLE = """
        {
           "data": {
               "username": "new_user",
               "fullName": "Иван Иванов",
               "password": "SecurePass123!",
               "email": "user@example.com",
               "phoneNumber": "+1234567890",
               "groupNumber": "ГР-01",
               "university": "Мой Университет"
           }
        }
        """;

    public static final String REGISTER_BAD_EXAMPLE = """
        {
            "username": "short",
            "fullName": "",
            "password": "123",
            "email": "invalid-email",
            "phoneNumber": "123",
            "groupNumber": "",
            "university": ""
        }
        """;

    public static final String REGISTER200_RESPONSE = """
        {
            "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        }
        """;

    public static final String REGISTER400_RESPONSE = """
        {
            "systemName": "auth-service",
            "applicationName": "university-auth",
            "timestamp": "2024-01-15T12:34:56.789",
            "errors": [
                "data.username: Username cannot be blank",
                "data.password: Password must be at least 8 characters long",
                "data.email: Email format is invalid"
            ]
        }""";

    public static final String REGISTER409_RESPONSE = """
        {
            "systemName": "auth-service",
            "applicationName": "university-auth",
            "error": "User with this email already exists"
        }
        """;

    public static final String REGISTER500_RESPONSE = """
        {
            "systemName": "auth-service",
            "applicationName": "university-auth",
            "error": "Internal server error"
        }
        """;

    // Refresh token examples
    public static final String REFRESH_TOKEN_EXAMPLE = """
        {
           "data": {
              "refreshToken": "your_refresh_token"
           }
        }
        """;

    public static final String REFRESH_TOKEN200_RESPONSE = "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"";

    public static final String REFRESH_TOKEN401_RESPONSE = """
        {
            "systemName": "auth-service",
            "applicationName": "university-auth",
            "error": "Invalid refresh token"
        }
        """;

    public static final String REFRESH_TOKEN500_RESPONSE = """
        {
            "systemName": "auth-service",
            "applicationName": "university-auth",
            "error": "Internal Server Error"
        }
        """;

    // Cookie examples
    public static final String ACCESS_TOKEN_COOKIE =
            "access_token=eyJhbGci...; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=900";

    public static final String REFRESH_TOKEN_COOKIE =
            "refresh_token=eyJhbGci...; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=2592000";

    // Logout examples
    public static final String LOGOUT404_RESPONSE =
            """
            {
                "systemName": "Schedule Auth",
                "applicationName": "tchs",
                "error": "No user found with refresh token"
            }
            """;
}
