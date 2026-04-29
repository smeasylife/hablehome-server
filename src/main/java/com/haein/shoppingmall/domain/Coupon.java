package com.haein.shoppingmall.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private Integer discountValue;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    protected Coupon() {
    }

    public Coupon(String name, DiscountType discountType, Integer discountValue, LocalDateTime startTime, LocalDateTime endTime) {
        this.name = name;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
