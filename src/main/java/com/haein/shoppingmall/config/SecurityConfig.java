package com.haein.shoppingmall.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haein.shoppingmall.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

@Configuration
public class SecurityConfig {

    private static final String CSRF_TOKEN_INVALID = "CSRF_TOKEN_INVALID";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .securityContext(context -> context.securityContextRepository(securityContextRepository()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin/login", "/admin/admin.css").permitAll()
                        .requestMatchers(HttpMethod.POST, "/admin/login").permitAll()
                        .requestMatchers("/admin-api/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/admin/**", "/admin").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/items", "/items/*").permitAll()
                        .requestMatchers("/signup/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/kakao/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/items", "/*/comment", "/answer/*", "/coupon").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/items/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/items/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/*/cart", "/*/like", "/*/review", "/question").authenticated()
                        .requestMatchers(HttpMethod.GET, "/cart", "/auth/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/cart", "/*/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                handleAuthenticationError(request, response, objectMapper))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                handleAccessDenied(request, response, objectMapper, accessDeniedException))
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeError(HttpServletResponse response, ObjectMapper objectMapper, int status, String message) throws java.io.IOException {
        writeError(response, objectMapper, status, null, message);
    }

    private void writeError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            int status,
            String code,
            String message
    ) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        if (code == null) {
            objectMapper.writeValue(response.getWriter(), ErrorResponse.of(message));
            return;
        }
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(code, message));
    }

    private void handleAuthenticationError(
            HttpServletRequest request,
            HttpServletResponse response,
            ObjectMapper objectMapper
    ) throws java.io.IOException {
        if (isAdminPageRequest(request)) {
            response.sendRedirect("/admin/login");
            return;
        }
        writeError(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다");
    }

    private void handleAccessDenied(
            HttpServletRequest request,
            HttpServletResponse response,
            ObjectMapper objectMapper,
            AccessDeniedException accessDeniedException
    ) throws java.io.IOException {
        if (isCsrfFailure(accessDeniedException)) {
            writeError(
                    response,
                    objectMapper,
                    HttpServletResponse.SC_FORBIDDEN,
                    CSRF_TOKEN_INVALID,
                    "CSRF token is missing or invalid"
            );
            return;
        }
        if (isAdminPageRequest(request)) {
            response.sendRedirect("/admin/login");
            return;
        }
        writeError(response, objectMapper, HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다");
    }

    private boolean isCsrfFailure(AccessDeniedException accessDeniedException) {
        return accessDeniedException instanceof InvalidCsrfTokenException
                || accessDeniedException instanceof MissingCsrfTokenException;
    }

    private boolean isAdminPageRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.equals("/admin") || (uri.startsWith("/admin/") && !uri.startsWith("/admin-api/"));
    }
}
