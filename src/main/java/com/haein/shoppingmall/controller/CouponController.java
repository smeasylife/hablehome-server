package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.dto.CouponRequest;
import com.haein.shoppingmall.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/coupon")
    public String createCoupon(@Valid @RequestBody CouponRequest request) {
        couponService.createCoupon(request);
        return "Coupon Saved";
    }
}
