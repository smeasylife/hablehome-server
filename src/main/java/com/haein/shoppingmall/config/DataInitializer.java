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
