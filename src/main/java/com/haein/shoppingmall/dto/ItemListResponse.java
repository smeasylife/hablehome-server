package com.haein.shoppingmall.dto;

public record ItemListResponse(
        Long id,
        String name,
        Integer price,
        Integer salePrice,
        String color,
        String pictureUrl,
        boolean like
) {
}
