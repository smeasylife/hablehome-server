package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.dto.CartItemResponse;
import com.haein.shoppingmall.dto.CartSelectionRequest;
import com.haein.shoppingmall.dto.ReviewCommentRequest;
import com.haein.shoppingmall.dto.ReviewRequest;
import com.haein.shoppingmall.service.CartService;
import com.haein.shoppingmall.service.LikeService;
import com.haein.shoppingmall.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActivityController {

    private final CartService cartService;
    private final LikeService likeService;
    private final ReviewService reviewService;

    public ActivityController(CartService cartService, LikeService likeService, ReviewService reviewService) {
        this.cartService = cartService;
        this.likeService = likeService;
        this.reviewService = reviewService;
    }

    @PostMapping("/{itemId}/cart")
    public ResponseEntity<Void> addCart(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        cartService.addCart(itemId, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cart")
    public List<CartItemResponse> findCartItems(
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        return cartService.findCartItems(memberId);
    }

    @DeleteMapping("/cart")
    public ResponseEntity<Void> removeSelectedCartItems(
            @Valid @RequestBody CartSelectionRequest request,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        cartService.removeSelectedCartItems(request.cartIds(), memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{itemId}/like")
    public ResponseEntity<Void> like(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        likeService.like(itemId, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{itemId}/review")
    public ResponseEntity<Void> createReview(
            @PathVariable Long itemId,
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        reviewService.createReview(itemId, request, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{reviewId}/comment")
    public ResponseEntity<Void> createComment(@PathVariable Long reviewId, @Valid @RequestBody ReviewCommentRequest request) {
        reviewService.createComment(reviewId, request.comment());
        return ResponseEntity.ok().build();
    }

}
