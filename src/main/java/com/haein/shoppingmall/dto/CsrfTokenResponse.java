package com.haein.shoppingmall.dto;

public record CsrfTokenResponse(
        String headerName,
        String parameterName,
        String token
) {
}
