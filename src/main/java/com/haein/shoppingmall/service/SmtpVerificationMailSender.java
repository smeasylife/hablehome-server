package com.haein.shoppingmall.service;

import com.haein.shoppingmall.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "true")
public class SmtpVerificationMailSender implements VerificationMailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpVerificationMailSender.class);

    private final JavaMailSender mailSender;
    private final String host;
    private final int port;
    private final String username;
    private final String fromAddress;
    private final String fromName;

    public SmtpVerificationMailSender(
            JavaMailSender mailSender,
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.username:}") String username,
            @Value("${app.mail.from}") String fromAddress,
            @Value("${app.mail.from-name:HABLE}") String fromName
    ) {
        this.mailSender = mailSender;
        this.host = host;
        this.port = port;
        this.username = username;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @PostConstruct
    public void logMailConfiguration() {
        log.info(
                "SMTP verification mail sender enabled. host={}, port={}, usernameConfigured={}, from={}",
                host,
                port,
                !username.isBlank(),
                fromAddress
        );
    }

    @Override
    public void sendVerificationCode(String email, String code) {
        try {
            log.info("Preparing signup verification email. to={}, from={}, host={}, port={}", email, fromAddress, host, port);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(email);
            helper.setFrom(new InternetAddress(fromAddress, fromName, StandardCharsets.UTF_8.name()));
            helper.setSubject("[HABLE] 이메일 인증번호");
            helper.setText("""
                    안녕하세요, HABLE입니다.

                    회원가입 이메일 인증번호는 %s 입니다.
                    인증번호는 5분 동안만 사용할 수 있습니다.

                    본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                    """.formatted(code), false);

            log.info("Sending signup verification email via SMTP. to={}", email);
            mailSender.send(message);
            log.info("Signup verification email sent successfully. to={}", email);
        } catch (MessagingException | UnsupportedEncodingException | MailException exception) {
            log.error("Failed to send signup verification email. to={}, from={}, host={}, port={}", email, fromAddress, host, port, exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "인증 메일 전송에 실패했습니다", exception);
        }
    }
}
