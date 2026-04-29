package com.haein.shoppingmall.dto;

import java.time.LocalDateTime;

public record QuestionResponse(Long id, String title, String content, String answer, LocalDateTime createdAt) {
}
