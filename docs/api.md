# Backend API Documentation

이 문서는 Spring MVC 컨트롤러, DTO, Security 설정 기준으로 작성했습니다.

## 공통 정보

- Base URL(local): `http://localhost:8080`
- 인증: Spring Security 세션 인증, `JSESSIONID` 쿠키
- CSRF: `CookieCsrfTokenRepository.withHttpOnlyFalse()` 사용
- 상태 변경 요청: CSRF 토큰 필요
- CSRF 검증 실패 응답: `403` + `code: "CSRF_TOKEN_INVALID"`
- CORS: `app.cors.allowed-origins`에 등록된 origin만 credential 요청 허용
- JSON null: Jackson `default-property-inclusion: non_null`

## 공통 에러 응답

```json
{
  "success": false,
  "data": null,
  "code": "CSRF_TOKEN_INVALID",
  "message": "오류 메시지"
}
```

`code`는 선택 필드이며, CSRF 토큰 누락/불일치처럼 클라이언트가 분기해야 하는 오류에만 내려갈 수 있습니다.

주요 상태 코드:

| 상태 | 의미 |
|---|---|
| 400 | validation 실패 또는 잘못된 비즈니스 요청 |
| 401 | 인증 필요 또는 로그인 실패 |
| 403 | 권한 부족, 구매하지 않은 상품 리뷰 작성, CSRF 토큰 누락/불일치 |
| 404 | 상품/주문/질문/리뷰 없음 |
| 409 | 이미 가입된 이메일 |
| 500 | 처리되지 않은 서버 오류 또는 필수 외부 설정 누락 |

## 권한 요약

| 권한 | 접근 가능 |
|---|---|
| Anonymous | `GET /health`, 상품 조회, 회원가입, 로그인, 카카오 로그인, CSRF 토큰 |
| Authenticated | 장바구니, 좋아요, 리뷰 작성, 문의 작성, 주문, 내 정보, 로그아웃 |
| `ROLE_ADMIN` | 상품 CUD, 쿠폰 생성, 문의 답변, 리뷰 답변, 관리자 페이지/API |

## 인증 API

### `GET /auth/csrf`

CSRF 토큰을 조회합니다.

Response:

```json
{
  "headerName": "X-XSRF-TOKEN",
  "parameterName": "_csrf",
  "token": "..."
}
```

### `POST /auth/login`

Request:

```json
{
  "email": "user@example.com",
  "password": "password"
}
```

Validation:

- `email`: email 형식, not blank
- `password`: not blank

Response:

```json
{
  "memberId": 1,
  "nickname": "테스트회원",
  "email": "user@example.com",
  "role": "ROLE_USER"
}
```

### `GET /auth/me`

현재 로그인 사용자를 반환합니다. 인증 필요.

Response: `AuthMemberResponse`

### `POST /auth/logout`

현재 세션을 무효화하고 `JSESSIONID` 만료 쿠키를 내려줍니다. 인증 필요.

Response: `204 No Content`

### `POST /auth/kakao/login`

카카오 authorization code를 서버에서 토큰으로 교환하고 사용자 정보를 조회한 뒤 로그인 처리합니다.

Request:

```json
{
  "code": "authorization-code",
  "redirectUri": "http://localhost:5173/login"
}
```

필수 설정:

- `KAKAO_REST_API_KEY`
- `KAKAO_CLIENT_SECRET`은 카카오 앱에서 client secret을 켠 경우만 필요

## 회원가입 API

### `POST /signup/send-code?email={email}`

회원가입 이메일 인증번호를 발송합니다.

Validation:

- `email`: email 형식, not blank

비즈니스 규칙:

- 이미 가입된 이메일이면 `409`
- 인증번호는 6자리 숫자
- 인증번호 만료 시간은 5분
- local 기본값에서는 메일 발송 대신 로그에 남길 수 있음

Response: `인증 번호 전송 성공`

### `POST /signup/verify-code`

Request:

```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

Response: `인증 성공`

### `POST /signup`

Request:

```json
{
  "nickname": "테스트회원",
  "email": "user@example.com",
  "password": "password1",
  "phoneNumber": "010-0000-0000"
}
```

Validation:

- `nickname`: not blank
- `email`: email 형식, not blank
- `password`: 8자 이상 64자 이하, 영문자와 숫자 각각 1개 이상 포함
- `phoneNumber`: optional

비즈니스 규칙:

- 이메일 인증이 완료되어야 가입 가능
- 가입 시 `ROLE_USER`, point `0`, `LOCAL` credential 생성

Response: `204 No Content`

## 상품 API

### `GET /items?page={page}`

상품 목록을 조회합니다. 비로그인 접근 가능하며, 로그인 상태면 각 상품의 `like` 여부가 반영됩니다.

Response:

```json
[
  {
    "id": 1,
    "name": "클린 코튼 차렵이불",
    "price": 89000,
    "salePrice": 69000,
    "color": "White",
    "pictureUrl": "https://...",
    "like": false,
    "categories": ["NEW", "BEST"]
  }
]
```

서버 규칙:

- 페이지당 20개
- `createdAt` 내림차순

### `GET /items/search?keyword={keyword}`

상품명 기준으로 상품을 검색합니다. 비로그인 접근 가능하며, 로그인 상태면 각 상품의 `like` 여부가 반영됩니다.

Request:

- `keyword`: 상품명 검색어. 앞뒤 공백은 제거되며, 빈 검색어는 빈 배열을 반환합니다.

Response:

```json
[
  {
    "id": 1,
    "name": "클린 코튼 차렵이불",
    "price": 89000,
    "salePrice": 69000,
    "color": "White",
    "pictureUrl": "https://...",
    "like": false,
    "categories": ["NEW", "BEST"]
  }
]
```

서버 규칙:

- 상품명 부분 일치, 대소문자 무시
- `createdAt` 내림차순

### `GET /items/{itemId}`

상품 상세를 조회합니다.

Response:

```json
{
  "itemId": 1,
  "name": "클린 코튼 차렵이불",
  "price": 89000,
  "salePrice": 69000,
  "shippingPrice": 3000,
  "size": "S / Q / K",
  "color": "White",
  "information": "상품 설명",
  "itemPictures": [{ "url": "https://..." }],
  "categories": ["NEW", "BEST"],
  "like": false,
  "reviews": [],
  "questions": []
}
```

### `POST /items`

관리자 전용 상품 생성 API입니다.

Request:

```json
{
  "name": "상품명",
  "price": 10000,
  "salePrice": 9000,
  "shippingPrice": 3000,
  "size": "S / Q / K",
  "color": "White",
  "information": "상품 설명",
  "pictureUrls": ["https://..."],
  "categories": ["NEW", "BEST"]
}
```

Validation:

- 문자열 필드는 not blank
- 가격 필드는 0 이상
- `pictureUrls`, `categories`는 not empty

Response: 생성된 상품 ID, status `201 Created`

### `PUT /items/{itemId}`

관리자 전용 상품 수정 API입니다. Request는 `POST /items`와 동일합니다.

### `DELETE /items/{itemId}`

관리자 전용 상품 삭제 API입니다.

삭제 시 관련 장바구니, 좋아요, 리뷰, 문의를 먼저 삭제합니다.

## 홍보 배너 API

### `GET /promo-banners`

홈 화면 홍보 배너를 노출 순서대로 조회합니다.

Response:

```json
[
  {
    "id": 1,
    "largeText": "하루 끝을 더 부드럽게",
    "smallText": "클린 코튼 차렵이불과 함께 침실의 계절감을 바꿔보세요.",
    "imageUrl": "/images/banners/1/001.png",
    "buttonLabel": "상품 보기",
    "itemId": 1,
    "itemName": "클린 코튼 차렵이불",
    "linkUrl": "/items/1",
    "displayOrder": 0
  }
]
```

## 장바구니와 좋아요 API

### `POST /{itemId}/cart`

로그인 필요. 상품을 장바구니에 담습니다.

Request:

```json
{
  "color": "White",
  "size": "S / Q / K",
  "quantity": 1
}
```

Validation:

- `quantity`: optional, 입력 시 1 이상

비즈니스 규칙:

- request body가 없어도 가능
- 색상/사이즈가 비어 있으면 상품 기본값 사용
- 같은 상품, 회원, 색상, 사이즈 조합이 있으면 수량 증가

Response: `204 No Content`

### `GET /cart`

로그인 필요. 현재 사용자의 장바구니를 최신순으로 반환합니다.

Response:

```json
[
  {
    "cartId": 1,
    "itemId": 1,
    "name": "클린 코튼 차렵이불",
    "price": 89000,
    "salePrice": 69000,
    "color": "White",
    "size": "S / Q / K",
    "quantity": 1,
    "pictureUrl": "https://..."
  }
]
```

### `DELETE /cart`

로그인 필요. 선택 장바구니 항목을 삭제합니다.

Request:

```json
{
  "cartIds": [1, 2]
}
```

Validation:

- `cartIds`: not empty

### `POST /{itemId}/like`

로그인 필요. 상품 좋아요를 생성합니다. 이미 좋아요한 상품이면 아무 작업 없이 성공합니다.

Response: `201 Created`

### `DELETE /{itemId}/like`

로그인 필요. 상품 좋아요를 취소합니다.

Response: `204 No Content`

## 주문 API

### `POST /orders`

로그인 필요. 주문을 생성합니다.

Request:

```json
{
  "cartIds": [1, 2],
  "couponId": null,
  "usedPoint": 0,
  "shippingAddress": {
    "recipientName": "홍길동",
    "phoneNumber": "010-0000-0000",
    "zipCode": "12345",
    "address1": "서울시 ...",
    "address2": "101호"
  }
}
```

또는 직접 주문:

```json
{
  "items": [
    {
      "itemId": 1,
      "color": "White",
      "size": "S / Q / K",
      "quantity": 1
    }
  ],
  "shippingAddress": {
    "recipientName": "홍길동",
    "phoneNumber": "010-0000-0000",
    "zipCode": "12345",
    "address1": "서울시 ...",
    "address2": "101호"
  }
}
```

비즈니스 규칙:

- `cartIds`와 `items` 중 정확히 하나만 있어야 함
- 배송지는 필수
- 직접 주문의 `quantity`는 1 이상
- 상품 금액은 `salePrice > 0`이면 `salePrice`, 아니면 `price`
- 50,000원 이상 무료배송, 그 외 기본 배송비 3,000원
- 포인트는 보유 포인트와 결제 금액을 초과할 수 없음
- 장바구니 주문 성공 시 선택한 장바구니 항목 삭제

Response: `OrderResponse`, status `201 Created`

```json
{
  "orderId": 1,
  "orderNumber": "ORD-20260502153000123",
  "status": "ORDERED",
  "createdAt": "2026-05-02T15:30:00.123",
  "shippingAddress": {
    "recipientName": "홍길동",
    "phoneNumber": "010-0000-0000",
    "zipCode": "12345",
    "address1": "서울시 ...",
    "address2": "101호"
  },
  "items": [
    {
      "orderItemId": 1,
      "itemId": 1,
      "itemName": "클린 코튼 차렵이불",
      "color": "White",
      "size": "S / Q / K",
      "unitPrice": 69000,
      "quantity": 1,
      "totalPrice": 69000
    }
  ],
  "amount": {
    "itemTotalAmount": 69000,
    "shippingFee": 0,
    "couponDiscountAmount": 0,
    "pointDiscountAmount": 0,
    "paymentAmount": 69000
  }
}
```

### `GET /orders`

로그인 필요. 현재 사용자의 주문 목록을 최신순으로 조회합니다.

### `GET /orders/{orderId}`

로그인 필요. 현재 사용자의 단일 주문을 조회합니다.

### `POST /orders/{orderId}/cancel`

로그인 필요. 현재 사용자의 주문을 취소합니다.

비즈니스 규칙:

- `SHIPPING`, `DELIVERED` 상태는 취소 불가
- 취소 시 사용 포인트 복구

## 리뷰 API

### `POST /{itemId}/review`

로그인 필요. 구매한 상품에 리뷰를 작성합니다.

Request:

```json
{
  "content": "좋아요",
  "rating": 5,
  "productOption": "White / S",
  "imageUrls": ["https://..."]
}
```

Validation:

- `content`: not blank
- `rating`: 1 이상 5 이하. null이면 서비스에서 5로 처리

비즈니스 규칙:

- `ORDERED`, `PAID`, `SHIPPING`, `DELIVERED` 상태의 주문 이력이 있어야 리뷰 작성 가능

Response: `201 Created`

### `POST /{reviewId}/comment`

관리자 전용 리뷰 답변 생성/수정 API입니다.

Request:

```json
{
  "comment": "관리자 답변"
}
```

Response: `200 OK`

## 문의 API

### `POST /question`

로그인 필요. 상품 문의를 작성합니다.

Request:

```json
{
  "itemId": 1,
  "title": "배송 문의",
  "content": "언제 배송되나요?"
}
```

Validation:

- `title`: not blank
- `content`: not blank

Response: `201 Created`

### `POST /answer/{questionId}?answer={answer}`

관리자 전용 레거시 문의 답변 API입니다. 새 관리자 REST API는 `/admin-api/questions/{questionId}/answer`를 우선 사용하세요.

## 쿠폰 API

### `POST /coupon`

관리자 전용 쿠폰 생성 API입니다.

Request:

```json
{
  "name": "봄 할인",
  "type": "PERCENT",
  "value": 10,
  "startTime": "2026-05-01T00:00:00",
  "endTime": "2026-05-31T23:59:59"
}
```

Validation:

- `name`: not blank
- `type`: `PERCENT` 또는 `FIXED_AMOUNT`, not null
- `value`: 0 이상, not null
- `startTime`, `endTime`: not null

Response: `Coupon Saved`

## 관리자 REST API

Base path: `/admin-api`

모든 API는 `ROLE_ADMIN` 권한이 필요합니다.

### `GET /admin-api/questions`

관리자 문의 목록을 최신순으로 조회합니다.

Response item:

```json
{
  "id": 1,
  "itemId": 1,
  "itemName": "클린 코튼 차렵이불",
  "nickname": "테스트회원",
  "title": "배송 문의",
  "content": "언제 배송되나요?",
  "answer": null,
  "createdAt": "2026-05-02T15:30:00"
}
```

### `PUT /admin-api/questions/{questionId}/answer`

Request:

```json
{
  "content": "답변 내용"
}
```

### `DELETE /admin-api/questions/{questionId}/answer`

문의 답변을 삭제합니다.

### `GET /admin-api/reviews`

관리자 리뷰 목록을 최신순으로 조회합니다.

### `PUT /admin-api/reviews/{reviewId}/comment`

리뷰 관리자 답변을 생성/수정합니다.

Request:

```json
{
  "content": "답변 내용"
}
```

### `DELETE /admin-api/reviews/{reviewId}/comment`

리뷰 관리자 답변을 삭제합니다.

## 관리자 페이지

Thymeleaf 기반 관리자 화면입니다.

| Method | Path | 설명 |
|---|---|---|
| `GET` | `/admin/login` | 관리자 로그인 페이지 |
| `POST` | `/admin/login` | 관리자 로그인 처리 |
| `POST` | `/admin/logout` | 관리자 로그아웃 |
| `GET` | `/admin` | 대시보드 |
| `GET` | `/admin/banners` | 홍보 배너 목록 |
| `GET` | `/admin/banners/new` | 홍보 배너 생성 폼 |
| `POST` | `/admin/banners` | 홍보 배너 생성 |
| `GET` | `/admin/banners/{bannerId}/edit` | 홍보 배너 수정 폼 |
| `POST` | `/admin/banners/{bannerId}` | 홍보 배너 수정 |
| `POST` | `/admin/banners/{bannerId}/delete` | 홍보 배너 삭제 |
| `GET` | `/admin/products` | 상품 목록 |
| `GET` | `/admin/products/new` | 상품 생성 폼 |
| `POST` | `/admin/products` | 상품 생성 |
| `GET` | `/admin/products/{itemId}/edit` | 상품 수정 폼 |
| `POST` | `/admin/products/{itemId}` | 상품 수정 |
| `POST` | `/admin/products/{itemId}/delete` | 상품 삭제 |
| `GET` | `/admin/questions` | 문의 관리 |
| `POST` | `/admin/questions/{questionId}/answer` | 문의 답변 저장 |
| `POST` | `/admin/questions/{questionId}/answer/delete` | 문의 답변 삭제 |
| `GET` | `/admin/reviews` | 리뷰 관리 |
| `POST` | `/admin/reviews/{reviewId}/comment` | 리뷰 답변 저장 |
| `POST` | `/admin/reviews/{reviewId}/comment/delete` | 리뷰 답변 삭제 |

## 헬스 체크

### `GET /health`

Response:

```json
{
  "status": "UP",
  "checkedAt": "2026-05-02T15:30:00Z"
}
```
