# Hablehome Server Docs

백엔드에서 필요한 정보를 빠르게 찾기 위한 문서 인덱스입니다.

## 문서 목록

| 문서 | 내용 | 이런 때 확인 |
|---|---|---|
| `docs/api.md` | REST API, 관리자 페이지/API, 인증/권한, 요청/응답 DTO | 프론트 연동, API 변경, 권한 확인 |
| `docs/db-schema.md` | JPA 엔티티 기준 테이블, 컬럼, 관계, enum | DB 변경, 마이그레이션, 데이터 분석 |
| `agent.md` | 프로젝트 도구, 환경 변수, 실행 방법, 코딩 원칙 | 백엔드 작업 시작 전 운영 규칙 확인 |
| `README.md` | 실행과 배포 환경의 기본 설명 | 로컬/도커 실행, 환경 변수 개요 |

## 데이터 위치 가이드

| 필요한 데이터 | 위치 |
|---|---|
| API 엔드포인트 | `src/main/java/com/haein/shoppingmall/controller` |
| 요청/응답 DTO와 validation | `src/main/java/com/haein/shoppingmall/dto` |
| 비즈니스 규칙 | `src/main/java/com/haein/shoppingmall/service` |
| DB 엔티티 | `src/main/java/com/haein/shoppingmall/domain` |
| Repository 쿼리 | `src/main/java/com/haein/shoppingmall/repository` |
| 보안/권한/CSRF | `src/main/java/com/haein/shoppingmall/config/SecurityConfig.java` |
| CORS | `src/main/java/com/haein/shoppingmall/config/WebConfig.java` |
| 초기 시드 데이터 | `src/main/java/com/haein/shoppingmall/config/DataInitializer.java` |
| 환경 설정 | `src/main/resources/application.yml`, `application-local.yml`, `application-prod.yml` |
| 관리자 템플릿 | `src/main/resources/templates/admin` |
| 테스트 | `src/test/java/com/haein/shoppingmall` |

## 프론트 문서와 맞춰 보기

프론트엔드가 실제로 호출하는 API와 화면 데이터 모델은 `../hablehome/docs`를 확인하세요. 백엔드가 계약의 원천이고, 프론트 문서는 소비자 관점의 요약입니다.
