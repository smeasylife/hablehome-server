package com.haein.shoppingmall.dto;

public record OrderAmountResponse(
        Integer itemTotalAmount,
        Integer shippingFee,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer paymentAmount
) {
}
