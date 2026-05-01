package com.haein.shoppingmall.dto;

public record OrderItemResponse(
        Long orderItemId,
        Long itemId,
        String itemName,
        String color,
        String size,
        Integer unitPrice,
        Integer quantity,
        Integer totalPrice
) {
}
