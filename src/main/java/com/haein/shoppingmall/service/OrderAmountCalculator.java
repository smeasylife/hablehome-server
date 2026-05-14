package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Coupon;
import com.haein.shoppingmall.domain.DiscountType;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.dto.OrderAmountResponse;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.repository.CouponRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OrderAmountCalculator {

    private static final int FREE_SHIPPING_THRESHOLD = 50_000;
    private static final int DEFAULT_SHIPPING_FEE = 3_000;

    private final CouponRepository couponRepository;

    public OrderAmountCalculator(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public OrderAmountResponse calculate(List<OrderLine> orderLines, Member member, Long couponId, Integer usedPoint) {
        if (orderLines.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "주문 상품을 선택해 주세요");
        }

        int itemTotalAmount = orderLines.stream()
                .mapToInt(line -> line.unitPrice() * line.quantity())
                .sum();
        int shippingFee = itemTotalAmount >= FREE_SHIPPING_THRESHOLD ? 0 : DEFAULT_SHIPPING_FEE;
        int couponDiscountAmount = calculateCouponDiscount(couponId, itemTotalAmount);
        int totalBeforePoint = itemTotalAmount + shippingFee - couponDiscountAmount;
        int pointDiscountAmount = validatePoint(member, usedPoint, totalBeforePoint);
        int paymentAmount = totalBeforePoint - pointDiscountAmount;

        return new OrderAmountResponse(
                itemTotalAmount,
                shippingFee,
                couponDiscountAmount,
                pointDiscountAmount,
                paymentAmount
        );
    }

    private int calculateCouponDiscount(Long couponId, int itemTotalAmount) {
        if (couponId == null) {
            return 0;
        }
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "쿠폰을 찾을 수 없습니다"));
        validateCouponPeriod(coupon);

        int discount;
        if (coupon.getDiscountType() == DiscountType.PERCENT) {
            discount = itemTotalAmount * coupon.getDiscountValue() / 100;
        } else {
            discount = coupon.getDiscountValue();
        }
        return Math.min(discount, itemTotalAmount);
    }

    private void validateCouponPeriod(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "아직 사용할 수 없는 쿠폰입니다");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "만료된 쿠폰입니다");
        }
    }

    private int validatePoint(Member member, Integer usedPoint, int totalBeforePoint) {
        int point = usedPoint == null ? 0 : usedPoint;
        if (point < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "사용 포인트는 0 이상이어야 합니다");
        }
        if (point > member.getPoint()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "보유 포인트를 초과했습니다");
        }
        if (point > totalBeforePoint) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "결제 금액보다 많은 포인트를 사용할 수 없습니다");
        }
        return point;
    }

    public record OrderLine(int unitPrice, int quantity) {
    }
}
