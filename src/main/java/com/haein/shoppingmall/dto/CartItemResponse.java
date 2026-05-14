package com.haein.shoppingmall.dto;

public record CartItemResponse(
        Long cartId,
        Long itemId,
        String name,
        Integer price,
        Integer salePrice,
        String color,
        String size,
        Integer additionalPrice,
        Integer quantity,
        String pictureUrl,
        Integer stockQuantity,
        boolean available
) {
}
