package com.haein.shoppingmall.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long id,
        String nickname,
        Integer rating,
        String productOption,
        List<String> imageUrls,
        String content,
        String adminComment,
        LocalDateTime createdAt
) {
}
