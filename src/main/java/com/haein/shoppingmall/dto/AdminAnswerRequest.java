package com.haein.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminAnswerRequest(@NotBlank String content) {
}
