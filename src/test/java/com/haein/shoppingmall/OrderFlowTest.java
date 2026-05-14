package com.haein.shoppingmall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haein.shoppingmall.domain.Credential;
import com.haein.shoppingmall.domain.IdentityProvider;
import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.domain.Role;
import com.haein.shoppingmall.dto.LoginRequest;
import com.haein.shoppingmall.repository.CredentialRepository;
import com.haein.shoppingmall.repository.ItemOptionRepository;
import com.haein.shoppingmall.repository.ItemRepository;
import com.haein.shoppingmall.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:shoppingmall-order-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.format_sql=false",
        "app.cors.allowed-origins=http://localhost:5173",
        "app.admin.email=admin-order@example.com",
        "app.admin.password=admin-password",
        "app.admin.nickname=테스트관리자"
})
@AutoConfigureMockMvc
class OrderFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemOptionRepository itemOptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String email;
    private Item item;

    @BeforeEach
    void setUp() {
        email = "order-" + System.nanoTime() + "@example.com";
        Member member = memberRepository.save(new Member("주문회원", email, "010-1234-5678", Role.ROLE_USER));
        credentialRepository.save(new Credential(IdentityProvider.LOCAL, passwordEncoder.encode("password1"), member));
        item = new Item("테스트 이불", 30_000, 20_000, 3_000, "S / Q", "White", "테스트 상품");
        item.addOption("White", "S", 3);
        item.addOption("White", "Q", 3, 20_000);
        item = itemRepository.save(item);
    }

    @Test
    void createsOrderFromSelectedCartItemsAndRemovesThem() throws Exception {
        HttpSession session = login();

        mockMvc.perform(post("/" + item.getId() + "/cart")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "color", "White",
                                "size", "Q",
                                "quantity", 2
                        ))))
                .andExpect(status().isNoContent());

        MvcResult cartResult = mockMvc.perform(get("/cart")
                        .session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].additionalPrice").value(20_000))
                .andExpect(jsonPath("$[0].salePrice").value(40_000))
                .andReturn();
        Long cartId = objectMapper.readTree(cartResult.getResponse().getContentAsString()).get(0).get("cartId").asLong();

        mockMvc.perform(post("/orders")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cartIds", List.of(cartId),
                                "usedPoint", 0,
                                "shippingAddress", shippingAddress()
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount.itemTotalAmount").value(80_000))
                .andExpect(jsonPath("$.amount.shippingFee").value(0))
                .andExpect(jsonPath("$.amount.paymentAmount").value(80_000))
                .andExpect(jsonPath("$.items[0].unitPrice").value(40_000))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        assertThat(itemOptionRepository.findByItemIdAndColorAndSize(item.getId(), "White", "Q").orElseThrow().getStockQuantity())
                .isEqualTo(1);

        MvcResult emptiedCartResult = mockMvc.perform(get("/cart")
                        .session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(objectMapper.readTree(emptiedCartResult.getResponse().getContentAsString()).isEmpty()).isTrue();
    }

    @Test
    void cartDoesNotDecreaseStockAndOrderRejectsInsufficientStock() throws Exception {
        HttpSession session = login();

        mockMvc.perform(post("/" + item.getId() + "/cart")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "color", "White",
                                "size", "Q",
                                "quantity", 3
                        ))))
                .andExpect(status().isNoContent());

        assertThat(itemOptionRepository.findByItemIdAndColorAndSize(item.getId(), "White", "Q").orElseThrow().getStockQuantity())
                .isEqualTo(3);

        mockMvc.perform(post("/orders")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "items", List.of(Map.of(
                                        "itemId", item.getId(),
                                        "color", "White",
                                        "size", "Q",
                                        "quantity", 4
                                )),
                                "usedPoint", 0,
                                "shippingAddress", shippingAddress()
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelOrderRestoresStockOnce() throws Exception {
        HttpSession session = login();

        MvcResult orderResult = mockMvc.perform(post("/orders")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "items", List.of(Map.of(
                                        "itemId", item.getId(),
                                        "color", "White",
                                        "size", "Q",
                                        "quantity", 2
                                )),
                                "usedPoint", 0,
                                "shippingAddress", shippingAddress()
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        Long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString()).get("orderId").asLong();

        assertThat(itemOptionRepository.findByItemIdAndColorAndSize(item.getId(), "White", "Q").orElseThrow().getStockQuantity())
                .isEqualTo(1);

        mockMvc.perform(post("/orders/" + orderId + "/cancel")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/orders/" + orderId + "/cancel")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf()))
                .andExpect(status().isOk());

        assertThat(itemOptionRepository.findByItemIdAndColorAndSize(item.getId(), "White", "Q").orElseThrow().getStockQuantity())
                .isEqualTo(3);
    }

    @Test
    void reviewRequiresOrderHistoryForItem() throws Exception {
        HttpSession session = login();

        mockMvc.perform(post("/" + item.getId() + "/review")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "content", "아직 구매 전 리뷰",
                                "rating", 5
                        ))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/orders")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "items", List.of(Map.of(
                                        "itemId", item.getId(),
                                        "color", "White",
                                        "size", "Q",
                                        "quantity", 1
                                )),
                                "usedPoint", 0,
                                "shippingAddress", shippingAddress()
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/" + item.getId() + "/review")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "content", "구매 후 리뷰",
                                "rating", 5,
                                "productOption", "White / Q",
                                "imageUrls", List.of()
                        ))))
                .andExpect(status().isCreated());
    }

    private HttpSession login() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "password1"))))
                .andExpect(status().isOk())
                .andReturn();
        return result.getRequest().getSession(false);
    }

    private Map<String, String> shippingAddress() {
        return Map.of(
                "recipientName", "홍길동",
                "phoneNumber", "010-1234-5678",
                "zipCode", "12345",
                "address1", "서울시 테스트구",
                "address2", "101호"
        );
    }
}
