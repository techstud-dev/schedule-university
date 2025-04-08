package com.techstud.schedule_university.auth.dto;

import com.techstud.schedule_university.auth.dto.request.LoginRecord;
import com.techstud.schedule_university.auth.dto.request.RegistrationRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(
        name = "ApiRequest",
        description = "Базовый запрос API с метаданными",
        type = "object",
        subTypes = {RegistrationRecord.class, LoginRecord.class}
)
public record ApiRequest<T>(
        @Schema(description = "Метаданные запроса")
        RequestMetadata metadata,

        @Schema(
                description = "Основные данные запроса",
                oneOf = {RegistrationRecord.class, LoginRecord.class}
        )
        @Valid T data
) {

    public ApiRequest(
            RequestMetadata metadata,
            @Valid T data
    ) {
        this.metadata = metadata != null ? metadata : new RequestMetadata(null);
        this.data = data;
    }

}
