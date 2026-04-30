package com.haein.shoppingmall.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminReviewResponse(
        Long id,
        Long itemId,
        String itemName,
        String nickname,
        Integer rating,
        String productOption,
        List<String> imageUrls,
        String content,
        String adminComment,
        LocalDateTime createdAt
) {
}
