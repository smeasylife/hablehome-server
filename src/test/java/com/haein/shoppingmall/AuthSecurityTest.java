package com.haein.shoppingmall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haein.shoppingmall.domain.IdentityProvider;
import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.ItemLike;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.domain.Question;
import com.haein.shoppingmall.domain.Review;
import com.haein.shoppingmall.dto.LoginRequest;
import com.haein.shoppingmall.dto.SignupRequest;
import com.haein.shoppingmall.dto.VerifyCodeRequest;
import com.haein.shoppingmall.repository.CredentialRepository;
import com.haein.shoppingmall.repository.ItemLikeRepository;
import com.haein.shoppingmall.repository.ItemRepository;
import com.haein.shoppingmall.repository.MemberRepository;
import com.haein.shoppingmall.repository.QuestionRepository;
import com.haein.shoppingmall.repository.ReviewRepository;
import com.haein.shoppingmall.service.MemberService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:shoppingmall-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=false",
        "app.cors.allowed-origins=http://localhost:5173",
        "app.admin.email=admin@example.com",
        "app.admin.password=admin-password",
        "app.admin.nickname=테스트관리자"
})
@AutoConfigureMockMvc
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemLikeRepository itemLikeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void signupStoresBCryptPassword() {
        String email = "signup-" + System.nanoTime() + "@example.com";
        String code = memberService.sendCode(email);
        memberService.verifyCode(new VerifyCodeRequest(email, code));

        memberService.signup(new SignupRequest("신규회원", email, "plain-password1", "010-1111-2222"));

        String encodedPassword = credentialRepository.findByMemberEmailAndIdentityProvider(email, IdentityProvider.LOCAL)
                .orElseThrow()
                .getPassword();
        assertThat(encodedPassword).isNotEqualTo("plain-password1");
        assertThat(passwordEncoder.matches("plain-password1", encodedPassword)).isTrue();
    }

    @Test
    void loginCreatesSessionAndMeReturnsCurrentMember() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user@example.com", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").exists())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(request().sessionAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        notNullValue()
                ))
                .andReturn();

        HttpSession session = loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/auth/me").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user@example.com", "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void csrfEndpointReturnsToken() throws Exception {
        mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void csrfFailureReturnsDedicatedErrorCode() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user@example.com", "password"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CSRF_TOKEN_INVALID"))
                .andExpect(jsonPath("$.message").value("CSRF token is missing or invalid"));
    }

    @Test
    void signupRejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequest(
                                "신규회원",
                                "weak-" + System.nanoTime() + "@example.com",
                                "password",
                                "010-1111-2222"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("비밀번호")));
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.checkedAt").isNotEmpty());
    }

    @Test
    void itemSearchReturnsProductsByPartialName() throws Exception {
        mockMvc.perform(get("/items/search").param("keyword", "이불"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name =~ /.*이불.*/)]").exists())
                .andExpect(jsonPath("$[?(@.name == '모달 스트라이프 침구 세트')]").doesNotExist());
    }

    @Test
    void itemSearchIgnoresCaseForEnglishNames() throws Exception {
        itemRepository.save(new Item("CASE Test Bedding", 10000, 9000, 3000, "Q", "White", "영문 검색 테스트"));

        mockMvc.perform(get("/items/search").param("keyword", "case test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'CASE Test Bedding')]").exists());
    }

    @Test
    void itemSearchReturnsEmptyArrayForBlankKeyword() throws Exception {
        mockMvc.perform(get("/items/search").param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void itemSearchReflectsCurrentMemberLikeState() throws Exception {
        Member member = memberRepository.findByEmail("user@example.com").orElseThrow();
        Item item = itemRepository.findAll().stream()
                .filter(candidate -> candidate.getName().contains("이불"))
                .findFirst()
                .orElseThrow();
        itemLikeRepository.save(new ItemLike(item, member));
        HttpSession session = login("user@example.com", "password");

        mockMvc.perform(get("/items/search")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .param("keyword", item.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(item.getId()))
                .andExpect(jsonPath("$[0].like").value(true));
    }

    @Test
    void protectedApisRequireLogin() throws Exception {
        List<org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder> requests = List.of(
                post("/1/cart").with(csrf()),
                post("/1/like").with(csrf()),
                post("/1/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "좋아요"))),
                post("/question")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "질문", "content", "내용")))
        );

        for (org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request : requests) {
            mockMvc.perform(request).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void userCannotAccessAdminApis() throws Exception {
        HttpSession session = login("user@example.com", "password");

        mockMvc.perform(post("/coupon")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(couponJson()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/items")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/1/comment")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("comment", "관리자 댓글"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/answer/1")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .param("answer", "관리자 답변"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminApis() throws Exception {
        HttpSession session = login("admin@example.com", "admin-password");
        Review review = createReview();
        Question question = createQuestion();

        mockMvc.perform(post("/coupon")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(couponJson()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/items")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson()))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/" + review.getId() + "/comment")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("comment", "관리자 댓글"))))
                .andExpect(status().isOk());
        mockMvc.perform(post("/answer/" + question.getId())
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .param("answer", "관리자 답변"))
                .andExpect(status().isOk());
    }

    @Test
    void adminPagesRequireLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void adminPagesRenderWithThymeleaf() throws Exception {
        HttpSession session = login("admin@example.com", "admin-password");
        Long itemId = itemRepository.findAll().get(0).getId();

        mockMvc.perform(get("/admin").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("운영 현황")));
        mockMvc.perform(get("/admin/products").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("상품 목록")));
        mockMvc.perform(get("/admin/products/new").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("상품 등록")));
        mockMvc.perform(get("/admin/products/" + itemId + "/edit").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("상품 수정")));
        mockMvc.perform(get("/admin/questions").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("QnA 답변")));
        mockMvc.perform(get("/admin/reviews").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("리뷰 답변")));
    }

    @Test
    void logoutClearsAuthentication() throws Exception {
        HttpSession session = login("user@example.com", "password");

        mockMvc.perform(post("/auth/logout")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    private HttpSession login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return result.getRequest().getSession(false);
    }

    private String couponJson() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "name", "테스트 쿠폰 " + System.nanoTime(),
                "type", "FIXED_AMOUNT",
                "value", 1000,
                "startTime", LocalDateTime.now(),
                "endTime", LocalDateTime.now().plusDays(7)
        ));
    }

    private String itemJson() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "name", "테스트 상품 " + System.nanoTime(),
                "price", 10000,
                "salePrice", 9000,
                "shippingPrice", 3000,
                "size", "Q",
                "color", "White",
                "information", "테스트 상품 설명",
                "pictureUrls", List.of("https://example.com/item.jpg"),
                "categories", List.of("NEW")
        ));
    }

    private Review createReview() {
        Item item = itemRepository.findAll().get(0);
        Member member = memberRepository.findByEmail("user@example.com").orElseThrow();
        return reviewRepository.save(new Review("리뷰", item, member));
    }

    private Question createQuestion() {
        Item item = itemRepository.findAll().get(0);
        Member member = memberRepository.findByEmail("user@example.com").orElseThrow();
        return questionRepository.save(new Question("질문", "내용", item, member));
    }
}
