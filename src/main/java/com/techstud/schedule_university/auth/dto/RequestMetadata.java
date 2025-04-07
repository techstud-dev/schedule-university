package com.techstud.schedule_university.auth.dto;

import java.util.UUID;

public record RequestMetadata(
        String requestId
) {

    public RequestMetadata {
        requestId = requestId == null ? UUID.randomUUID().toString() : requestId;
    }

}
