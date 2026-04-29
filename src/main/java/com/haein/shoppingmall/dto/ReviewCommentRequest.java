package com.haein.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewCommentRequest(@NotBlank String comment) {
}
