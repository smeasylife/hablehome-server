package com.haein.shoppingmall.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CartSelectionRequest(
        @NotEmpty(message = "선택한 장바구니 상품이 없습니다")
        List<Long> cartIds
) {
}
