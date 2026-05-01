package com.haein.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;

public record ShippingAddressRequest(
        @NotBlank String recipientName,
        @NotBlank String phoneNumber,
        @NotBlank String zipCode,
        @NotBlank String address1,
        String address2
) {
}
