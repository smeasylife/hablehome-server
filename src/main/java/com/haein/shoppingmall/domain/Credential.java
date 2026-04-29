package com.haein.shoppingmall.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private IdentityProvider identityProvider;

    private String password;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    protected Credential() {
    }

    public Credential(IdentityProvider identityProvider, String password, Member member) {
        this.identityProvider = identityProvider;
        this.password = password;
        this.member = member;
    }

    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    public String getPassword() {
        return password;
    }

    public Member getMember() {
        return member;
    }

    public void changePassword(String password) {
        this.password = password;
    }
}
