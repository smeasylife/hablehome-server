package com.haein.shoppingmall.common;

public record ErrorResponse(boolean success, Object data, String message) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, null, message);
    }
}
