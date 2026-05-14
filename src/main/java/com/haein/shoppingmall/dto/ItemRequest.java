package com.haein.shoppingmall.dto;

import com.haein.shoppingmall.domain.CategoryName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record ItemRequest(
        @NotBlank String name,
        @NotNull @PositiveOrZero Integer price,
        @NotNull @PositiveOrZero Integer salePrice,
        @NotNull @PositiveOrZero Integer shippingPrice,
        @NotBlank String size,
        @NotBlank String color,
        @NotBlank String information,
        List<@NotBlank String> pictureUrls,
        @NotEmpty List<CategoryName> categories,
        @NotEmpty List<@Valid ItemOptionRequest> options
) {
}
