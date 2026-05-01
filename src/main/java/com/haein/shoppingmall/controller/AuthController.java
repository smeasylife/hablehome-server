package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.dto.AuthMemberResponse;
import com.haein.shoppingmall.dto.CsrfTokenResponse;
import com.haein.shoppingmall.dto.KakaoLoginRequest;
import com.haein.shoppingmall.dto.LoginRequest;
import com.haein.shoppingmall.security.AuthMember;
import com.haein.shoppingmall.service.AuthService;
import com.haein.shoppingmall.service.KakaoAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final KakaoAuthService kakaoAuthService;
    private final boolean sessionCookieSecure;
    private final String sessionCookieSameSite;
    private final String sessionCookieDomain;

    public AuthController(
            AuthService authService,
            KakaoAuthService kakaoAuthService,
            @Value("${server.servlet.session.cookie.secure:false}") boolean sessionCookieSecure,
            @Value("${server.servlet.session.cookie.same-site:lax}") String sessionCookieSameSite,
            @Value("${server.servlet.session.cookie.domain:}") String sessionCookieDomain
    ) {
        this.authService = authService;
        this.kakaoAuthService = kakaoAuthService;
        this.sessionCookieSecure = sessionCookieSecure;
        this.sessionCookieSameSite = sessionCookieSameSite;
        this.sessionCookieDomain = sessionCookieDomain;
    }

    @PostMapping("/login")
    public AuthMemberResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        return authService.login(request, servletRequest, servletResponse);
    }

    @GetMapping("/me")
    public AuthMemberResponse me(@AuthenticationPrincipal AuthMember authMember) {
        return authService.toResponse(authMember);
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getParameterName(), csrfToken.getToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredSessionCookie().toString());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/kakao/login")
    public AuthMemberResponse kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        return kakaoAuthService.login(request, servletRequest, servletResponse);
    }

    private ResponseCookie expiredSessionCookie() {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .httpOnly(true)
                .secure(sessionCookieSecure)
                .sameSite(sessionCookieSameSite)
                .maxAge(0);

        if (StringUtils.hasText(sessionCookieDomain)) {
            cookieBuilder.domain(sessionCookieDomain);
        }

        return cookieBuilder.build();
    }
}
