package com.haein.shoppingmall.dto;

import jakarta.validation.Valid;
import java.util.List;

public record OrderCreateRequest(
        List<Long> cartIds,
        List<@Valid OrderItemRequest> items,
        Long couponId,
        Integer usedPoint,
        @Valid ShippingAddressRequest shippingAddress
) {
}
