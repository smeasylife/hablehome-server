package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Credential;
import com.haein.shoppingmall.domain.IdentityProvider;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.domain.Role;
import com.haein.shoppingmall.dto.AuthMemberResponse;
import com.haein.shoppingmall.dto.KakaoLoginRequest;
import com.haein.shoppingmall.dto.KakaoTokenResponse;
import com.haein.shoppingmall.dto.KakaoUserResponse;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.repository.CredentialRepository;
import com.haein.shoppingmall.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class KakaoAuthService {

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_URL = "https://kapi.kakao.com/v2/user/me";

    private final CredentialRepository credentialRepository;
    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final RestClient restClient;
    private final String restApiKey;
    private final String clientSecret;

    public KakaoAuthService(
            CredentialRepository credentialRepository,
            MemberRepository memberRepository,
            AuthService authService,
            @Value("${app.kakao.rest-api-key:}") String restApiKey,
            @Value("${app.kakao.client-secret:}") String clientSecret
    ) {
        this.credentialRepository = credentialRepository;
        this.memberRepository = memberRepository;
        this.authService = authService;
        this.restClient = RestClient.create();
        this.restApiKey = restApiKey;
        this.clientSecret = clientSecret;
    }

    @Transactional
    public AuthMemberResponse login(
            KakaoLoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        if (!StringUtils.hasText(restApiKey)) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 REST API 키가 설정되지 않았습니다");
        }

        KakaoTokenResponse tokenResponse = requestToken(request);
        KakaoUserResponse userResponse = requestUser(tokenResponse.accessToken());
        Member member = findOrCreateMember(userResponse);
        return authService.loginMember(member, servletRequest, servletResponse);
    }

    private KakaoTokenResponse requestToken(KakaoLoginRequest request) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", restApiKey);
        body.add("redirect_uri", request.redirectUri());
        body.add("code", request.code());
        if (StringUtils.hasText(clientSecret)) {
            body.add("client_secret", clientSecret);
        }

        try {
            KakaoTokenResponse response = restClient.post()
                    .uri(KAKAO_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(KakaoTokenResponse.class);
            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "카카오 토큰을 발급받지 못했습니다");
            }
            return response;
        } catch (RestClientException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "카카오 로그인 인증에 실패했습니다");
        }
    }

    private KakaoUserResponse requestUser(String accessToken) {
        try {
            KakaoUserResponse response = restClient.get()
                    .uri(KAKAO_USER_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);
            if (response == null || response.id() == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "카카오 사용자 정보를 확인하지 못했습니다");
            }
            return response;
        } catch (RestClientException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "카카오 사용자 정보 조회에 실패했습니다");
        }
    }

    private Member findOrCreateMember(KakaoUserResponse userResponse) {
        String kakaoId = String.valueOf(userResponse.id());
        Optional<Credential> savedCredential = credentialRepository.findByIdentityProviderAndPassword(
                IdentityProvider.KAKAO,
                kakaoId
        );
        if (savedCredential.isPresent()) {
            return savedCredential.get().getMember();
        }

        String email = resolveEmail(userResponse, kakaoId);
        Optional<Member> savedMember = memberRepository.findByEmail(email);
        if (savedMember.isPresent()) {
            return savedMember.get();
        }

        Member member = memberRepository.save(new Member(resolveNickname(userResponse, kakaoId), email, null, Role.ROLE_USER));
        credentialRepository.save(new Credential(IdentityProvider.KAKAO, kakaoId, member));
        return member;
    }

    private String resolveEmail(KakaoUserResponse userResponse, String kakaoId) {
        KakaoUserResponse.KakaoAccount account = userResponse.kakaoAccount();
        if (account != null
                && StringUtils.hasText(account.email())
                && !Boolean.FALSE.equals(account.emailValid())
                && !Boolean.FALSE.equals(account.emailVerified())) {
            return account.email();
        }
        return "kakao-" + kakaoId + "@kakao.local";
    }

    private String resolveNickname(KakaoUserResponse userResponse, String kakaoId) {
        KakaoUserResponse.KakaoAccount account = userResponse.kakaoAccount();
        if (account != null
                && account.profile() != null
                && StringUtils.hasText(account.profile().nickname())) {
            return account.profile().nickname();
        }
        if (userResponse.properties() != null && StringUtils.hasText(userResponse.properties().get("nickname"))) {
            return userResponse.properties().get("nickname");
        }
        return "카카오회원" + kakaoId.substring(Math.max(0, kakaoId.length() - 4));
    }
}
