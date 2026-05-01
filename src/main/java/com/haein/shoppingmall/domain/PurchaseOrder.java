package com.haein.shoppingmall.domain;

import com.haein.shoppingmall.exception.BusinessException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;

@Entity
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Embedded
    private ShippingAddress shippingAddress;

    private Integer itemTotalAmount;

    private Integer shippingFee;

    private Integer couponDiscountAmount;

    private Integer pointDiscountAmount;

    private Integer paymentAmount;

    private LocalDateTime createdAt;

    private LocalDateTime canceledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    protected PurchaseOrder() {
    }

    public PurchaseOrder(
            String orderNumber,
            Member member,
            ShippingAddress shippingAddress,
            Integer itemTotalAmount,
            Integer shippingFee,
            Integer couponDiscountAmount,
            Integer pointDiscountAmount,
            Integer paymentAmount
    ) {
        this.orderNumber = orderNumber;
        this.member = member;
        this.status = OrderStatus.ORDERED;
        this.shippingAddress = shippingAddress;
        this.itemTotalAmount = itemTotalAmount;
        this.shippingFee = shippingFee;
        this.couponDiscountAmount = couponDiscountAmount;
        this.pointDiscountAmount = pointDiscountAmount;
        this.paymentAmount = paymentAmount;
        this.createdAt = LocalDateTime.now();
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
    }

    public void cancel() {
        if (status == OrderStatus.CANCELED) {
            return;
        }
        if (status == OrderStatus.SHIPPING || status == OrderStatus.DELIVERED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "배송이 시작된 주문은 취소할 수 없습니다");
        }
        status = OrderStatus.CANCELED;
        canceledAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Member getMember() {
        return member;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public Integer getItemTotalAmount() {
        return itemTotalAmount;
    }

    public Integer getShippingFee() {
        return shippingFee;
    }

    public Integer getCouponDiscountAmount() {
        return couponDiscountAmount;
    }

    public Integer getPointDiscountAmount() {
        return pointDiscountAmount;
    }

    public Integer getPaymentAmount() {
        return paymentAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
}
