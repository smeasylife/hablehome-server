package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.dto.OrderCreateRequest;
import com.haein.shoppingmall.dto.OrderResponse;
import com.haein.shoppingmall.security.AuthMember;
import com.haein.shoppingmall.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, authMember.getMemberId()));
    }

    @GetMapping
    public List<OrderResponse> findOrders(@AuthenticationPrincipal AuthMember authMember) {
        return orderService.findOrders(authMember.getMemberId());
    }

    @GetMapping("/{orderId}")
    public OrderResponse findOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return orderService.findOrder(orderId, authMember.getMemberId());
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return orderService.cancelOrder(orderId, authMember.getMemberId());
    }
}
