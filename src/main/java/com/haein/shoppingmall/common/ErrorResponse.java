package com.haein.shoppingmall.common;

public record ErrorResponse(boolean success, Object data, String code, String message) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, null, null, message);
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(false, null, code, message);
    }
}
