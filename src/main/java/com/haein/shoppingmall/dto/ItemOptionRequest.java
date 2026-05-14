package com.haein.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ItemOptionRequest(
        @NotBlank String color,
        @NotBlank String size,
        @NotNull @PositiveOrZero Integer stockQuantity,
        @PositiveOrZero Integer additionalPrice
) {
}
