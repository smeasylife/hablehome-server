package com.haein.shoppingmall.dto;

import java.util.List;

public record ItemDetailResponse(
        Long itemId,
        String name,
        Integer price,
        Integer salePrice,
        Integer shippingPrice,
        String size,
        String color,
        String information,
        List<ItemPictureResponse> itemPictures,
        List<ReviewResponse> reviews,
        List<QuestionResponse> questions
) {
}
