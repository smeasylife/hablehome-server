package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
