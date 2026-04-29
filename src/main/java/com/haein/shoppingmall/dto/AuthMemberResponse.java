package com.haein.shoppingmall.dto;

import com.haein.shoppingmall.domain.Role;

public record AuthMemberResponse(
        Long memberId,
        String nickname,
        String email,
        Role role
) {
}
