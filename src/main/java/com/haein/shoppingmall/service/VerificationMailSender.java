package com.haein.shoppingmall.service;

public interface VerificationMailSender {

    void sendVerificationCode(String email, String code);
}
