package com.haein.shoppingmall.dto;

import com.haein.shoppingmall.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String orderNumber,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime canceledAt,
        ShippingAddressResponse shippingAddress,
        List<OrderItemResponse> items,
        OrderAmountResponse amount
) {
}
