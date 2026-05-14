package com.haein.shoppingmall.dto;

public record ItemOptionResponse(
        Long optionId,
        String color,
        String size,
        Integer stockQuantity,
        Integer additionalPrice,
        boolean soldOut
) {
}
