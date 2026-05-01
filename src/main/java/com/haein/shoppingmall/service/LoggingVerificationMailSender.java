package com.haein.shoppingmall.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingVerificationMailSender implements VerificationMailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingVerificationMailSender.class);

    @PostConstruct
    public void logMailConfiguration() {
        log.warn("SMTP mail sending is disabled. Set APP_MAIL_ENABLED=true to send real signup verification emails.");
    }

    @Override
    public void sendVerificationCode(String email, String code) {
        log.info("Mail sending is disabled. Signup verification code for {} is {}", email, code);
    }
}
