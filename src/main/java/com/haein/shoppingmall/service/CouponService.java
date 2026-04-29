package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Coupon;
import com.haein.shoppingmall.dto.CouponRequest;
import com.haein.shoppingmall.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    public void createCoupon(CouponRequest request) {
        couponRepository.save(new Coupon(
                request.name(),
                request.type(),
                request.value(),
                request.startTime(),
                request.endTime()
        ));
    }
}
