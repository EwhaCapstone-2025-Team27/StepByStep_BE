package com.dragon.stepbystep.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private String message;
    private String errorCode;
    private T data;
    private int code;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, null, data, 200);
    }

    public static <T> ApiResponse<T> error(String errorCode, String message, int code) {
        return new ApiResponse<>("error", errorCode, errorCode, null, code);
    }

}