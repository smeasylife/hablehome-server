package com.haein.shoppingmall.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank String nickname,
        @Email @NotBlank String email,
        @NotBlank String password,
        String phoneNumber
) {
}
