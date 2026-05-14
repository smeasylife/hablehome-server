package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Cart;
import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.domain.OrderItem;
import com.haein.shoppingmall.domain.OrderStatus;
import com.haein.shoppingmall.domain.PurchaseOrder;
import com.haein.shoppingmall.domain.ShippingAddress;
import com.haein.shoppingmall.dto.OrderAmountResponse;
import com.haein.shoppingmall.dto.OrderCreateRequest;
import com.haein.shoppingmall.dto.OrderItemRequest;
import com.haein.shoppingmall.dto.OrderItemResponse;
import com.haein.shoppingmall.dto.OrderResponse;
import com.haein.shoppingmall.dto.ShippingAddressRequest;
import com.haein.shoppingmall.dto.ShippingAddressResponse;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.repository.CartRepository;
import com.haein.shoppingmall.repository.PurchaseOrderRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final List<OrderStatus> REVIEW_ALLOWED_STATUSES = List.of(
            OrderStatus.ORDERED,
            OrderStatus.PAID,
            OrderStatus.SHIPPING,
            OrderStatus.DELIVERED
    );

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final CartRepository cartRepository;
    private final ItemService itemService;
    private final MemberService memberService;
    private final OrderAmountCalculator orderAmountCalculator;

    public OrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            CartRepository cartRepository,
            ItemService itemService,
            MemberService memberService,
            OrderAmountCalculator orderAmountCalculator
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.cartRepository = cartRepository;
        this.itemService = itemService;
        this.memberService = memberService;
        this.orderAmountCalculator = orderAmountCalculator;
    }

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, Long memberId) {
        Member member = memberService.findCurrentMember(memberId);
        ShippingAddress shippingAddress = toAddress(request.shippingAddress());
        List<OrderSourceLine> sourceLines = findOrderLines(request, member);
        List<OrderAmountCalculator.OrderLine> amountLines = sourceLines.stream()
                .map(line -> new OrderAmountCalculator.OrderLine(line.unitPrice(), line.quantity()))
                .toList();
        OrderAmountResponse amount = orderAmountCalculator.calculate(
                amountLines,
                member,
                request.couponId(),
                request.usedPoint()
        );

        PurchaseOrder order = new PurchaseOrder(
                createOrderNumber(),
                member,
                shippingAddress,
                amount.itemTotalAmount(),
                amount.shippingFee(),
                amount.couponDiscountAmount(),
                amount.pointDiscountAmount(),
                amount.paymentAmount()
        );
        sourceLines.forEach(line -> {
            itemService.decreaseStock(line.item().getId(), line.color(), line.size(), line.quantity());
            order.addOrderItem(new OrderItem(
                    order,
                    line.item(),
                    line.color(),
                    line.size(),
                    line.unitPrice(),
                    line.quantity()
            ));
        });

        member.usePoint(amount.pointDiscountAmount());
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        if (request.cartIds() != null && !request.cartIds().isEmpty()) {
            cartRepository.deleteByIdInAndMemberId(request.cartIds(), member.getId());
        }
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findOrders(Long memberId) {
        Member member = memberService.findCurrentMember(memberId);
        return purchaseOrderRepository.findByMemberIdOrderByCreatedAtDesc(member.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findOrder(Long orderId, Long memberId) {
        Member member = memberService.findCurrentMember(memberId);
        return toResponse(findMemberOrder(orderId, member.getId()));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long memberId) {
        Member member = memberService.findCurrentMember(memberId);
        PurchaseOrder order = findMemberOrder(orderId, member.getId());
        boolean alreadyCanceled = order.getStatus() == OrderStatus.CANCELED;
        order.cancel();
        if (!alreadyCanceled) {
            order.getOrderItems().forEach(orderItem -> itemService.restoreStock(
                    orderItem.getItem().getId(),
                    orderItem.getColor(),
                    orderItem.getSize(),
                    orderItem.getQuantity()
            ));
            member.restorePoint(order.getPointDiscountAmount());
        }
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public boolean hasPurchasedItem(Long memberId, Long itemId) {
        return purchaseOrderRepository.existsByMemberIdAndOrderItemsItemIdAndStatusIn(
                memberId,
                itemId,
                REVIEW_ALLOWED_STATUSES
        );
    }

    private List<OrderSourceLine> findOrderLines(OrderCreateRequest request, Member member) {
        boolean hasCartIds = request.cartIds() != null && !request.cartIds().isEmpty();
        boolean hasDirectItems = request.items() != null && !request.items().isEmpty();
        if (hasCartIds == hasDirectItems) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "장바구니 항목 또는 직접 주문 상품 중 하나만 선택해 주세요");
        }

        if (hasCartIds) {
            if (request.cartIds().stream().anyMatch(java.util.Objects::isNull)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "장바구니 항목 ID를 입력해 주세요");
            }
            List<Cart> carts = cartRepository.findByIdInAndMemberId(request.cartIds(), member.getId());
            if (carts.size() != Set.copyOf(request.cartIds()).size()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "선택한 장바구니 항목을 찾을 수 없습니다");
            }
            return carts.stream()
                    .map(cart -> new OrderSourceLine(
                            cart.getItem(),
                            cart.getColor(),
                            cart.getSize(),
                            cart.getQuantity(),
                            itemService.effectivePrice(cart.getItem(), cart.getColor(), cart.getSize())
                    ))
                    .toList();
        }

        return request.items().stream()
                .map(this::toDirectOrderLine)
                .toList();
    }

    private OrderSourceLine toDirectOrderLine(OrderItemRequest request) {
        Item item = itemService.findItemEntity(request.itemId());
        return new OrderSourceLine(
                item,
                blankToDefault(request.color(), item.getColor()),
                blankToDefault(request.size(), item.getSize()),
                request.quantity(),
                itemService.effectivePrice(
                        item,
                        blankToDefault(request.color(), item.getColor()),
                        blankToDefault(request.size(), item.getSize())
                )
        );
    }

    private PurchaseOrder findMemberOrder(Long orderId, Long memberId) {
        return purchaseOrderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다"));
    }

    private ShippingAddress toAddress(ShippingAddressRequest request) {
        if (request == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "배송지를 입력해 주세요");
        }
        return new ShippingAddress(
                request.recipientName(),
                request.phoneNumber(),
                request.zipCode(),
                request.address1(),
                request.address2()
        );
    }

    private String blankToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private String createOrderNumber() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private OrderResponse toResponse(PurchaseOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getCanceledAt(),
                toAddressResponse(order.getShippingAddress()),
                order.getOrderItems().stream()
                        .map(this::toItemResponse)
                        .toList(),
                new OrderAmountResponse(
                        order.getItemTotalAmount(),
                        order.getShippingFee(),
                        order.getCouponDiscountAmount(),
                        order.getPointDiscountAmount(),
                        order.getPaymentAmount()
                )
        );
    }

    private ShippingAddressResponse toAddressResponse(ShippingAddress address) {
        return new ShippingAddressResponse(
                address.getRecipientName(),
                address.getPhoneNumber(),
                address.getZipCode(),
                address.getAddress1(),
                address.getAddress2()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getItem().getId(),
                orderItem.getItemName(),
                orderItem.getColor(),
                orderItem.getSize(),
                orderItem.getUnitPrice(),
                orderItem.getQuantity(),
                orderItem.getTotalPrice()
        );
    }

    private record OrderSourceLine(Item item, String color, String size, int quantity, int unitPrice) {
    }
}
