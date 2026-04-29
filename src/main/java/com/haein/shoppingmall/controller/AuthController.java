package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.dto.KakaoLoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/kakao/login")
    public String kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        return "Login successful";
    }
}
