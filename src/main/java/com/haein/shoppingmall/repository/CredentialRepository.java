package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
}
