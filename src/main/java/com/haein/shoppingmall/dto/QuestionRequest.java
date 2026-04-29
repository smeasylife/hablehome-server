package com.haein.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;

public record QuestionRequest(
        Long itemId,
        @NotBlank String title,
        @NotBlank String content
) {
}
