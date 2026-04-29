package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.dto.SignupRequest;
import com.haein.shoppingmall.dto.VerifyCodeRequest;
import com.haein.shoppingmall.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class SignupController {

    private final MemberService memberService;

    public SignupController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        memberService.signup(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/signup/send-code")
    public String sendCode(@RequestParam @Email @NotBlank String email) {
        memberService.sendCode(email);
        return "인증 번호 전송 성공";
    }

    @PostMapping("/signup/verify-code")
    public String verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        memberService.verifyCode(request);
        return "인증 성공";
    }
}
