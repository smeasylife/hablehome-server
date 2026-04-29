package com.haein.shoppingmall.config;

import com.haein.shoppingmall.domain.Category;
import com.haein.shoppingmall.domain.CategoryName;
import com.haein.shoppingmall.domain.Credential;
import com.haein.shoppingmall.domain.IdentityProvider;
import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.domain.Role;
import com.haein.shoppingmall.repository.CategoryRepository;
import com.haein.shoppingmall.repository.CredentialRepository;
import com.haein.shoppingmall.repository.ItemRepository;
import com.haein.shoppingmall.repository.MemberRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(SeedService seedService) {
        return args -> seedService.seed();
    }

    @Component
    static class SeedService {

        private final CategoryRepository categoryRepository;
        private final ItemRepository itemRepository;
        private final MemberRepository memberRepository;
        private final CredentialRepository credentialRepository;
        private final PasswordEncoder passwordEncoder;
        private final String adminEmail;
        private final String adminPassword;
        private final String adminNickname;

        SeedService(
                CategoryRepository categoryRepository,
                ItemRepository itemRepository,
                MemberRepository memberRepository,
                CredentialRepository credentialRepository,
                PasswordEncoder passwordEncoder,
                @Value("${app.admin.email:}") String adminEmail,
                @Value("${app.admin.password:}") String adminPassword,
                @Value("${app.admin.nickname:관리자}") String adminNickname
        ) {
            this.categoryRepository = categoryRepository;
            this.itemRepository = itemRepository;
            this.memberRepository = memberRepository;
            this.credentialRepository = credentialRepository;
            this.passwordEncoder = passwordEncoder;
            this.adminEmail = adminEmail;
            this.adminPassword = adminPassword;
            this.adminNickname = adminNickname;
        }

        @Transactional
        void seed() {
            Arrays.stream(CategoryName.values())
                    .filter(name -> !categoryRepository.existsByName(name))
                    .forEach(name -> categoryRepository.save(new Category(name)));

            if (!memberRepository.existsByEmail("user@example.com")) {
                Member member = memberRepository.save(new Member("테스트회원", "user@example.com", "010-0000-0000", Role.ROLE_USER));
                credentialRepository.save(new Credential(IdentityProvider.LOCAL, passwordEncoder.encode("password"), member));
            }

            seedAdmin();

            if (itemRepository.count() == 0) {
                createItem(
                        "클린 코튼 차렵이불",
                        89000,
                        69000,
                        "White",
                        "부드러운 코튼 원단과 깔끔한 호텔 침구 무드의 사계절 차렵이불입니다.",
                        List.of("NEW", "BEST"),
                        "https://images.unsplash.com/photo-1616627561950-9f746e330187?auto=format&fit=crop&w=900&q=80"
                );
                createItem(
                        "모달 스트라이프 침구 세트",
                        129000,
                        99000,
                        "Gray",
                        "차분한 스트라이프 패턴과 매끈한 모달 촉감이 특징인 침구 세트입니다.",
                        List.of("BEST", "SALE"),
                        "https://images.unsplash.com/photo-1631049035182-249067d7618e?auto=format&fit=crop&w=900&q=80"
                );
                createItem(
                        "호텔식 사계절 이불",
                        118000,
                        118000,
                        "Ivory",
                        "가볍고 포근한 충전재를 사용한 미니멀 호텔식 사계절 이불입니다.",
                        List.of("NEW"),
                        "https://images.unsplash.com/photo-1618220179428-22790b461013?auto=format&fit=crop&w=900&q=80"
                );
                createItem(
                        "여름 리플 홑이불",
                        59000,
                        47000,
                        "Blue",
                        "시원한 촉감의 리플 원단으로 제작한 여름용 홑이불입니다.",
                        List.of("SUMMER", "SALE"),
                        "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=900&q=80"
                );
                createItem(
                        "프리미엄 구스 이불",
                        219000,
                        179000,
                        "Charcoal",
                        "보온성과 복원력이 좋은 프리미엄 구스 충전재를 사용했습니다.",
                        List.of("WINTER", "BEST"),
                        "https://images.unsplash.com/photo-1615874694520-474822394e73?auto=format&fit=crop&w=900&q=80"
                );
                createItem(
                        "워싱 면 패드 세트",
                        76000,
                        64000,
                        "Beige",
                        "자연스러운 워싱 텍스처와 안정적인 누빔감을 제공하는 패드 세트입니다.",
                        List.of("SPRING", "FALL"),
                        "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=900&q=80"
                );
            }
        }

        private void createItem(String name, int price, int salePrice, String color, String information, List<String> categoryNames, String pictureUrl) {
            Item item = new Item(name, price, salePrice, 3000, "S / Q / K", color, information);
            item.replacePictures(List.of(pictureUrl));
            item.replaceCategories(categoryNames.stream()
                    .map(CategoryName::valueOf)
                    .map(categoryName -> categoryRepository.findByName(categoryName).orElseThrow())
                    .toList());
            itemRepository.save(item);
        }

        private void seedAdmin() {
            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                return;
            }

            Member member = memberRepository.findByEmail(adminEmail)
                    .orElseGet(() -> memberRepository.save(new Member(adminNickname, adminEmail, null, Role.ROLE_ADMIN)));
            member.updateNickname(adminNickname);
            member.changeRole(Role.ROLE_ADMIN);

            Credential credential = credentialRepository.findByMemberEmailAndIdentityProvider(adminEmail, IdentityProvider.LOCAL)
                    .orElseGet(() -> credentialRepository.save(new Credential(IdentityProvider.LOCAL, passwordEncoder.encode(adminPassword), member)));
            credential.changePassword(passwordEncoder.encode(adminPassword));
        }
    }
}
