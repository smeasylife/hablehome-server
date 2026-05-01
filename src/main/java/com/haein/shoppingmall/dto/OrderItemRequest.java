package com.haein.shoppingmall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull Long itemId,
        String color,
        String size,
        @NotNull @Min(1) Integer quantity
) {
}
