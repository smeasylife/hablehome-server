package com.haein.shoppingmall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ReviewRequest(
        @NotBlank String content,
        @Min(1) @Max(5) Integer rating,
        String productOption,
        List<String> imageUrls
) {
}
