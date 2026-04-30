package com.haein.shoppingmall.dto;

import java.time.LocalDateTime;

public record AdminQuestionResponse(
        Long id,
        Long itemId,
        String itemName,
        String nickname,
        String title,
        String content,
        String answer,
        LocalDateTime createdAt
) {
}
