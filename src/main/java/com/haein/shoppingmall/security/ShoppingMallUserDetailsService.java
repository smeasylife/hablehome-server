package com.haein.shoppingmall.security;

import com.haein.shoppingmall.domain.Credential;
import com.haein.shoppingmall.domain.IdentityProvider;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.repository.CredentialRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ShoppingMallUserDetailsService implements UserDetailsService {

    private final CredentialRepository credentialRepository;

    public ShoppingMallUserDetailsService(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Credential credential = credentialRepository.findByMemberEmailAndIdentityProvider(username, IdentityProvider.LOCAL)
                .orElseThrow(() -> new UsernameNotFoundException("회원 정보를 찾을 수 없습니다"));
        Member member = credential.getMember();
        return new AuthMember(
                member.getId(),
                member.getEmail(),
                credential.getPassword(),
                member.getNickname(),
                member.getRole()
        );
    }
}
