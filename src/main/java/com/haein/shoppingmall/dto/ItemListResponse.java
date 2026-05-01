package com.haein.shoppingmall.dto;

import java.util.List;

public record ItemListResponse(
        Long id,
        String name,
        Integer price,
        Integer salePrice,
        String color,
        String pictureUrl,
        boolean like,
        List<String> categories
) {
}
