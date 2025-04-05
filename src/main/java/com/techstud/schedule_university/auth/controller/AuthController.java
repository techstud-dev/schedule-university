package com.techstud.schedule_university.auth.controller;

import com.techstud.schedule_university.auth.dto.ApiRequest;
import com.techstud.schedule_university.auth.dto.request.LoginDTO;
import com.techstud.schedule_university.auth.dto.request.RefreshTokenRequest;
import com.techstud.schedule_university.auth.dto.request.RegisterDTO;
import com.techstud.schedule_university.auth.dto.response.SuccessAuthenticationDTO;
import com.techstud.schedule_university.auth.service.LoginService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;
    private final RegistrationService registrationService;
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
    @PostMapping("/login")
    public ResponseEntity<SuccessAuthenticationDTO> login(
            @RequestBody @Valid ApiRequest<@Valid LoginDTO> dto) throws Exception {
        log.info("Incoming login request, id: {}", dto.metadata().requestId());
        SuccessAuthenticationDTO response = loginService.processLogin(dto.data());

        List<ResponseCookie> cookies = cookieUtil.createAuthCookies(
                response.token(),
                response.refreshToken()
        );

        log.info("Outgoing login response, id: {}", dto.metadata().requestId());
        return responseUtil.okWithCookies(response, cookies.toArray(ResponseCookie[]::new));
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = """
        Регистрирует нового пользователя в системе с указанными учетными данными.
        После успешной регистрации возвращает JWT токены доступа.
        Пароль должен соответствовать требованиям безопасности.
        """,
            tags = {"Authentication"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
            Все поля обязательны для заполнения.
            Метаданные (metadata) генерируются автоматически при отсутствии.
            """,
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Успешный запрос",
                                            value = AuthApiExamples.REGISTER_EXAMPLE,
                                            description = "Пример с автоматически сгенерированными метаданными"
                                    ),
                                    @ExampleObject(
                                            name = "Неверный запрос",
                                            value = AuthApiExamples.REGISTER_BAD_EXAMPLE,
                                            description = "Пример с ошибками валидации в данных"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Пользователь успешно зарегистрирован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessAuthenticationDTO.class),
                                    examples = @ExampleObject(value = AuthApiExamples.REGISTER200_RESPONSE)
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
                            description = "Некорректные данные запроса",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = AuthApiExamples.REGISTER400_RESPONSE,
                                            description = "Ошибки валидации в полях данных"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Конфликт данных",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.REGISTER409_RESPONSE)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = AuthApiExamples.REGISTER500_RESPONSE)
                            )
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<SuccessAuthenticationDTO> register(
            @RequestBody @Valid ApiRequest<@Valid RegisterDTO> dto) throws Exception {
        log.info("Incoming register request, username: {}, request id: {}",
                dto.data().username(), dto.metadata().requestId());
        SuccessAuthenticationDTO response = registrationService.processRegister(dto.data());

        List<ResponseCookie> cookies = cookieUtil.createAuthCookies(
                response.token(),
                response.refreshToken()
        );

        log.info("Outgoing register response, username {}, request id: {}",
                dto.data().username(), dto.metadata().requestId());
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
    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(
            @RequestBody @Valid ApiRequest<@Valid RefreshTokenRequest> dto) throws Exception {
        log.info("Incoming refresh token request, id: {}", dto.metadata().requestId());
        String accessToken = refreshTokenService.refreshToken(dto.data().refreshToken());

        ResponseCookie accessTokenCookie = cookieUtil.createAccessTokenCookie(accessToken);

        log.info("Outgoing refresh token response, id: {}", dto.metadata().requestId());
        return responseUtil.okWithCookies(accessToken, accessTokenCookie);
    }
}
