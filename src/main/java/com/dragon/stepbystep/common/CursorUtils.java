package com.dragon.stepbystep.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class CursorUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private CursorUtils() {
    }

    public static String encode(Object value) {
        try {
            byte[] jsonBytes = OBJECT_MAPPER.writeValueAsBytes(value);
            return Base64.getEncoder().encodeToString(jsonBytes);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("커서를 생성할 수 없습니다.", e);
        }
    }

    public static <T> T decode(String cursor, Class<T> type) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(cursor.getBytes(StandardCharsets.UTF_8));
            return OBJECT_MAPPER.readValue(decoded, type);
        } catch (IllegalArgumentException | IOException e) {   // ← IOException 반드시 처리
            throw new IllegalArgumentException("유효하지 않은 커서입니다.");
        }
    }
}