package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.IdentityProvider;
import com.haein.shoppingmall.domain.Credential;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<Credential, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<Credential> findByMemberEmailAndIdentityProvider(String email, IdentityProvider identityProvider);
}
