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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public MemberService(MemberRepository memberRepository, CredentialRepository credentialRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String sendCode(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        verificationCodes.put(email, new VerificationCode(code, LocalDateTime.now().plusMinutes(5), false));
        log.info("Signup verification code for {} is {}", email, code);
        return code;
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
        credentialRepository.save(new Credential(IdentityProvider.LOCAL, passwordEncoder.encode(request.password()), member));
        verificationCodes.remove(request.email());
    }

    @Transactional(readOnly = true)
    public Member findCurrentMember(Long memberId) {
        if (memberId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다"));
    }

    private record VerificationCode(String code, LocalDateTime expiresAt, boolean verified) {
    }
}
