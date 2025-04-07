package com.techstud.schedule_university.auth.controller;

import com.techstud.schedule_university.auth.aspect.RateLimitKeyType;
import com.techstud.schedule_university.auth.aspect.RateLimited;
import com.techstud.schedule_university.auth.dto.ApiRequest;
import com.techstud.schedule_university.auth.dto.request.*;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationDTO;
import com.techstud.schedule_university.auth.exception.UserExistsException;
import com.techstud.schedule_university.auth.service.LoginService;
import com.techstud.schedule_university.auth.service.LogoutService;
import com.techstud.schedule_university.auth.service.RefreshTokenService;
import com.techstud.schedule_university.auth.service.RegistrationService;
import com.techstud.schedule_university.auth.swagger.AuthApiExamples;
import com.techstud.schedule_university.auth.util.CookieUtil;
import com.techstud.schedule_university.auth.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;
    private final RegistrationService registrationService;
    private final LogoutService logoutService;
    private final CookieUtil cookieUtil;
    private final ResponseUtil responseUtil;

    @Operation(
            summary = "Аутентификация пользователя",
            description = """
            Аутентифицирует пользователя по учетным данным.
            Поддерживает вход по username, email или phoneNumber.
            При успехе возвращает JWT токены доступа.
            """,
            tags = {"Authentication"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Учетные данные пользователя",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginDTO.class),
                            examples = @ExampleObject(value = AuthApiExamples.LOGIN_EXAMPLE)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная аутентификация",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessAuthenticationDTO.class),
                                    examples = @ExampleObject(value = AuthApiExamples.LOGIN200_RESPONSE)
                            ),
                            headers = {
                                    @Header(
                                            name = "Set-Cookie",
                                            description = "Access token cookie",
                                            schema = @Schema(type = "string", example = AuthApiExamples.ACCESS_TOKEN_COOKIE)
                                    ),
                                    @Header(
                                            name = "Set-Cookie",
                                            description = "Refresh token cookie",
                                            schema = @Schema(type = "string", example = AuthApiExamples.REFRESH_TOKEN_COOKIE)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверный формат запроса",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.LOGIN400_RESPONSE)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неверные учетные данные",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.LOGIN401_RESPONSE)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.LOGIN404_RESPONSE)
                            )
                    )
            }
    )
    @RateLimited(limit = 3, interval = 100, keyType = RateLimitKeyType.USER_ID)
    @PostMapping("/login")
    public ResponseEntity<SuccessAuthenticationDTO> login(
            @RequestBody @Valid ApiRequest<@Valid LoginDTO> dto) throws Exception {
        log.info("Incoming login request, id: {}", dto.metadata().requestId());
        SuccessAuthenticationDTO response = loginService.processLogin(dto.data());
        List<ResponseCookie> cookies = cookieUtil.createAuthCookies(response.token(), response.refreshToken());
        log.info("Outgoing login response, id: {}", dto.metadata().requestId());
        return responseUtil.okWithCookies(response, cookies.toArray(ResponseCookie[]::new));
    }

    @Operation(
            summary = "Инициация регистрации",
            description = """
        Начинает процесс регистрации нового пользователя./
        Отправляет код подтверждения на указанный email.
        Лимит: 10 запросов в 30 секунд с одного IP.
        """,
            tags = {"Authentication"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
            Все поля обязательны для заполнения.
            Пароль должен содержать минимум 8 символов, включая цифры и буквы.
            """,
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Пример корректного запроса",
                                            value = AuthApiExamples.REGISTER_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "Пример невалидного запроса",
                                            value = AuthApiExamples.REGISTER_BAD_EXAMPLE
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Код подтверждения отправлен",
                            content = @Content(
                                    mediaType = "text/plain",
                                    examples = @ExampleObject(value = "Sent confirmation code.")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Ошибка валидации данных",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.REGISTER400_RESPONSE))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Пользователь уже существует",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.REGISTER409_RESPONSE))
                    ),
                    @ApiResponse(
                            responseCode = "429",
                            description = "Превышен лимит запросов"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка сервера",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.REGISTER500_RESPONSE))
                    )
            }
    )
    @RateLimited(limit = 10, interval = 30, keyType = RateLimitKeyType.IP)
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody @Valid ApiRequest<@Valid RegisterDTO> request)
            throws MessagingException, UserExistsException {
        log.info("Incoming register request, id: {}", request.metadata().requestId());
        registrationService.startRegistration(request.data());
        log.info("Outgoing register response, id: {}", request.metadata().requestId());
        return ResponseEntity.ok("Sent confirmation code.");
    }

    @Operation(
            summary = "Подтверждение регистрации",
            description = """
        Завершает процесс регистрации с использованием кода подтверждения.
        Возвращает JWT токены доступа и обновления.
        Лимит: 5 запросов в 30 секунд с одного IP.
        """,
            tags = {"Authentication"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Код подтверждения из email",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "data": {
                            "code": "123456"
                        }
                    }"""
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная регистрация",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessAuthenticationDTO.class),
                                    examples = @ExampleObject(value = AuthApiExamples.REGISTER200_RESPONSE)
                            ),
                            headers = {
                                    @Header(
                                            name = "Set-Cookie",
                                            description = "Access token",
                                            schema = @Schema(example = AuthApiExamples.ACCESS_TOKEN_COOKIE)
                                    ),
                                    @Header(
                                            name = "Set-Cookie",
                                            description = "Refresh token",
                                            schema = @Schema(example = AuthApiExamples.REFRESH_TOKEN_COOKIE)
                                    )
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Неверный код подтверждения",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                        {
                            "error": "Invalid confirmation code"
                        }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Конфликт данных пользователя"
                    ),
                    @ApiResponse(
                            responseCode = "429",
                            description = "Превышен лимит запросов"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Ошибка сервера"
                    )
            }
    )
    @RateLimited(limit = 10, interval = 30, keyType = RateLimitKeyType.IP)
    @PostMapping("/confirm")
    public ResponseEntity<SuccessAuthenticationDTO> confirm(
            @RequestBody @Valid ApiRequest<@Valid ConfirmRegisterRequest> dto) throws Exception {
        log.info("Incoming register request, request id: {}", dto.metadata().requestId());
        SuccessAuthenticationDTO response = registrationService.completeRegistration(dto.data().code());
        List<ResponseCookie> cookies = cookieUtil.createAuthCookies(response.token(), response.refreshToken());
        log.info("Outgoing register response, request id: {}", dto.metadata().requestId());
        return responseUtil.okWithCookies(response, cookies.toArray(ResponseCookie[]::new));
    }

    @Operation(
            summary = "Обновление access токена",
            description = "Использует валидный refresh токен для получения нового access токена",
            tags = {"Authentication"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token для обновления",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = AuthApiExamples.REFRESH_TOKEN_EXAMPLE)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Access токен успешно обновлён",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.REFRESH_TOKEN200_RESPONSE)
                            ),
                            headers = @Header(
                                    name = "Set-Cookie",
                                    description = "Новый access token",
                                    schema = @Schema(type = "string", example = AuthApiExamples.ACCESS_TOKEN_COOKIE)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Невалидный refresh токен",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.REFRESH_TOKEN401_RESPONSE)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.REFRESH_TOKEN500_RESPONSE)
                            )
                    )
            }
    )
    @RateLimited(limit = 10, interval = 30, keyType = RateLimitKeyType.USER_ID)
    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(
            @RequestBody @Valid ApiRequest<@Valid RefreshTokenRequest> dto) throws Exception {
        log.info("Incoming refresh token request, id: {}", dto.metadata().requestId());
        String accessToken = refreshTokenService.refreshToken(dto.data().refreshToken());
        ResponseCookie accessTokenCookie = cookieUtil.createAccessTokenCookie(accessToken);
        log.info("Outgoing refresh token response, id: {}", dto.metadata().requestId());
        return responseUtil.okWithCookies(accessToken, accessTokenCookie);
    }

    @Operation(
            summary = "Выход пользователя из системы",
            description = "Завершает сессию пользователя, удаляя refresh token",
            tags = "Authentication",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Успешный выход из системы"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Невалидный запрос / Пользователь не найден",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                                "systemName": "Schedule Auth",
                                                "applicationName": "tchs",
                                                "error": "No user found with refresh token"
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @RateLimited
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody @Valid ApiRequest<@Valid LogoutRequest> request) throws Exception {
        log.info("logout request received, id: {}", request.metadata().requestId());
        logoutService.logout(request.data().refreshToken());
        log.info("Outgoing logout response, id: {}", request.metadata().requestId());
        return ResponseEntity.noContent().build();
    }
}
