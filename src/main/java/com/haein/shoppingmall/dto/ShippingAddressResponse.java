package com.haein.shoppingmall.dto;

public record ShippingAddressResponse(
        String recipientName,
        String phoneNumber,
        String zipCode,
        String address1,
        String address2
) {
}
