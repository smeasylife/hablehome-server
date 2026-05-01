package com.haein.shoppingmall.dto;

import jakarta.validation.constraints.Min;

public record CartRequest(
        String color,
        String size,
        @Min(1) Integer quantity
) {
}
