package com.techstud.schedule_university.auth.dto;

import com.techstud.schedule_university.auth.dto.request.LoginDTO;
import com.techstud.schedule_university.auth.dto.request.RegisterDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(
        name = "ApiRequest",
        description = "Базовый запрос API с метаданными",
        type = "object",
        subTypes = {RegisterDTO.class, LoginDTO.class}
)
public record ApiRequest<T>(
        @Schema(description = "Метаданные запроса")
        RequestMetadata metadata,

        @Schema(
                description = "Основные данные запроса",
                oneOf = {RegisterDTO.class, LoginDTO.class}
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
