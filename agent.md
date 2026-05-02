# Hablehome Server Agent Guide

이 문서는 백엔드 프로젝트에서 작업할 때 먼저 확인할 운영 규칙입니다.

## 프로젝트 개요

- 역할: HABLE 쇼핑몰 REST API와 관리자 페이지 서버
- 루트: `hablehome-server`
- 메인 클래스: `src/main/java/com/haein/shoppingmall/ShoppingMallApplication.java`
- 기본 local API URL: `http://localhost:8080`
- 인증: Spring Security 세션 + CSRF
- 관리자 UI: Thymeleaf templates under `src/main/resources/templates/admin`

## 사용 도구와 라이브러리

| 영역 | 도구 |
|---|---|
| 런타임 | Java 21 |
| 프레임워크 | Spring Boot 3.3.5 |
| 빌드 | Gradle |
| 웹 | Spring MVC, Spring Validation |
| 보안 | Spring Security, BCrypt |
| 데이터 | Spring Data JPA, Hibernate |
| DB | PostgreSQL, H2 runtime dependency |
| 템플릿 | Thymeleaf |
| 메일 | Spring Boot Starter Mail |
| 테스트 | Spring Boot Test, Spring Security Test, JUnit Platform |
| 컨테이너 | Dockerfile, docker-compose |

## 주요 명령어

```bash
./gradlew test
./gradlew bootRun
docker compose up --build
```

## 환경과 설정

| 파일 | 내용 |
|---|---|
| `src/main/resources/application.yml` | 공통 설정, 환경 변수 바인딩 |
| `src/main/resources/application-local.yml` | local 기본값, PostgreSQL localhost, `ddl-auto=update` |
| `src/main/resources/application-prod.yml` | production 설정, `ddl-auto=validate` 계열 |
| `docker-compose.yml` | 로컬 DB/API 실행 구성 |
| `Dockerfile` | Gradle build 후 JRE 이미지 실행 |

주요 환경 변수:

| 변수 | 용도 |
|---|---|
| `SPRING_DATASOURCE_URL` | DB URL |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL 모드 |
| `FRONTEND_ALLOWED_ORIGINS` | credential CORS 허용 origin 목록 |
| `APP_MAIL_ENABLED` | 회원가입 인증 메일 실제 발송 여부 |
| `SPRING_MAIL_*` | SMTP 설정 |
| `KAKAO_REST_API_KEY` | 카카오 로그인 REST API 키 |
| `KAKAO_CLIENT_SECRET` | 카카오 client secret 사용 시 설정 |
| `APP_ADMIN_EMAIL` | 관리자 계정 이메일 seed |
| `APP_ADMIN_PASSWORD` | 관리자 계정 비밀번호 seed |
| `APP_ADMIN_NICKNAME` | 관리자 닉네임 seed |
| `SESSION_COOKIE_SECURE` | 세션 쿠키 Secure 여부 |
| `SESSION_COOKIE_SAME_SITE` | 세션 쿠키 SameSite |

## 코딩 원칙

- 컨트롤러는 HTTP 계약과 DTO validation에 집중하고, 비즈니스 규칙은 service에 둡니다.
- 엔티티 직접 노출 대신 response DTO를 반환합니다.
- 사용자 입력 DTO에는 Bean Validation을 명시합니다.
- 인증 사용자는 `@AuthenticationPrincipal AuthMember`로 받고, 실제 회원 엔티티는 `MemberService.findCurrentMember`를 통해 조회합니다.
- 상태 변경 API는 CSRF 흐름을 깨지 않도록 유지합니다.
- 관리자 전용 기능은 `ROLE_ADMIN` 권한 규칙을 `SecurityConfig`와 함께 갱신합니다.
- DB 관계 변경 시 `docs/db-schema.md`와 필요하면 프론트 타입 문서를 함께 갱신합니다.
- API 경로/응답 변경 시 `docs/api.md`와 프론트 `src/api`, `src/types`를 함께 확인합니다.

## 패키지 안내

| 경로 | 내용 |
|---|---|
| `controller` | REST API와 관리자 페이지 라우트 |
| `dto` | 요청/응답 DTO, validation |
| `service` | 비즈니스 규칙, 트랜잭션 경계 |
| `domain` | JPA 엔티티와 enum |
| `repository` | Spring Data JPA repository |
| `security` | 인증 사용자 어댑터와 UserDetailsService |
| `config` | Security, CORS, seed data 설정 |
| `exception` | 비즈니스 예외와 전역 예외 처리 |
| `common` | 공통 응답 객체 |
| `resources/templates/admin` | Thymeleaf 관리자 페이지 |
| `resources/static/admin` | 관리자 정적 리소스 |
| `src/test` | 통합/보안 테스트 |

## 문서 안내

| 문서 | 어디서 확인할 내용 |
|---|---|
| `docs/index.md` | 문서 목록과 데이터 위치 빠른 안내 |
| `docs/api.md` | API 목록, 권한, request/response, 비즈니스 규칙 |
| `docs/db-schema.md` | 테이블, 컬럼, 관계, enum, 초기 데이터 |
| `README.md` | 실행, Docker Compose, 프로필, 배포 환경 개요 |

## 프론트엔드와 맞출 때

- 프론트 루트는 `../hablehome`입니다.
- 프론트 API 소비 문서는 `../hablehome/docs/api.md`를 확인합니다.
- 세션 쿠키를 쓰므로 CORS는 `allowCredentials(true)`와 정확한 origin 등록이 필요합니다.
- 프론트 개발 서버 기본 origin은 local 설정에 `http://localhost:5173`, `http://localhost:5174`, `http://localhost:3000`이 포함되어 있습니다.
