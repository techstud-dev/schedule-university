package com.techstud.schedule_university.fetcher.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Slf4j
public class FetcherHttpUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> HttpClientResponseHandler<T> createResponseHandler(TypeReference<T> typeReference) {
        return response -> processResponse(response, body -> MAPPER.readValue(body, typeReference));
    }

    public static <T> HttpClientResponseHandler<T> createResponseHandler(Class<T> responseType, boolean isHtml) {
        return response -> processResponse(response, body -> {
            if (isHtml) {
                @SuppressWarnings("unchecked")
                T result = (T) body;
                return result;
            } else {
                return MAPPER.readValue(body, responseType);
            }
        });
    }

    private static <T> T processResponse(ClassicHttpResponse response, BodyProcessor<T> bodyProcessor)
            throws IOException, ParseException {
        int statusCode = response.getCode();
        if (statusCode != 200) {
            log.error("Error while getting response, status code: {}", statusCode);
            throw new IOException("Unexpected response code: " + statusCode);
        }

        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        if (responseBody.isBlank()) {
            log.error("Response body is empty or null");
            throw new IOException("Empty or null response body");
        }

        try {
            return bodyProcessor.process(responseBody);
        } catch (Exception e) {
            log.error("Error processing response body: {}", responseBody, e);
            throw new IOException("Failed to process response body", e);
        }
    }

    @FunctionalInterface
    private interface BodyProcessor<T> {
        T process(String body) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }

    public static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
