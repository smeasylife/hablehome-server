package com.haein.shoppingmall.dto;

import com.haein.shoppingmall.domain.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

public record CouponRequest(
        @NotBlank String name,
        @NotNull DiscountType type,
        @NotNull @PositiveOrZero Integer value,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {
}
