package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.dto.AuthMemberResponse;
import com.haein.shoppingmall.dto.LoginRequest;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.security.AuthMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    public AuthMemberResponse login(LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            saveAuthentication(authentication, servletRequest, servletResponse);
            return toResponse((AuthMember) authentication.getPrincipal());
        } catch (BadCredentialsException exception) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다");
        }
    }

    public AuthMemberResponse loginMember(Member member, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        AuthMember authMember = new AuthMember(
                member.getId(),
                member.getEmail(),
                "",
                member.getNickname(),
                member.getRole()
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authMember,
                null,
                authMember.getAuthorities()
        );
        saveAuthentication(authentication, servletRequest, servletResponse);
        return toResponse(authMember);
    }

    public AuthMemberResponse toResponse(AuthMember authMember) {
        return new AuthMemberResponse(
                authMember.getMemberId(),
                authMember.getNickname(),
                authMember.getEmail(),
                authMember.getRole()
        );
    }

    private void saveAuthentication(
            Authentication authentication,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        if (servletRequest.getSession(false) != null) {
            servletRequest.changeSessionId();
        }
        securityContextRepository.saveContext(securityContext, servletRequest, servletResponse);
    }
}
