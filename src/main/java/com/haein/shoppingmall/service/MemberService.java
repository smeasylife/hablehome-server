package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Credential;
import com.haein.shoppingmall.domain.IdentityProvider;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.domain.Role;
import com.haein.shoppingmall.dto.SignupRequest;
import com.haein.shoppingmall.dto.VerifyCodeRequest;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.repository.CredentialRepository;
import com.haein.shoppingmall.repository.MemberRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final CredentialRepository credentialRepository;
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public MemberService(MemberRepository memberRepository, CredentialRepository credentialRepository) {
        this.memberRepository = memberRepository;
        this.credentialRepository = credentialRepository;
    }

    public void sendCode(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        verificationCodes.put(email, new VerificationCode(code, LocalDateTime.now().plusMinutes(5), false));
        log.info("Signup verification code for {} is {}", email, code);
    }

    public void verifyCode(VerifyCodeRequest request) {
        VerificationCode savedCode = verificationCodes.get(request.email());
        if (savedCode == null || savedCode.expiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "인증 번호가 만료되었습니다");
        }
        if (!savedCode.code().equals(request.code())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "인증 번호가 일치하지 않습니다");
        }
        verificationCodes.put(request.email(), new VerificationCode(savedCode.code(), savedCode.expiresAt(), true));
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다");
        }
        VerificationCode savedCode = verificationCodes.get(request.email());
        if (savedCode == null || !savedCode.verified()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이메일 인증이 필요합니다");
        }
        Member member = memberRepository.save(new Member(request.nickname(), request.email(), request.phoneNumber(), Role.ROLE_USER));
        credentialRepository.save(new Credential(IdentityProvider.LOCAL, hashPassword(request.password()), member));
        verificationCodes.remove(request.email());
    }

    @Transactional(readOnly = true)
    public Member findCurrentMember(Long memberId) {
        Long id = memberId == null ? 1L : memberId;
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다"));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private record VerificationCode(String code, LocalDateTime expiresAt, boolean verified) {
    }
}
