package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.domain.Review;
import com.haein.shoppingmall.domain.ReviewComment;
import com.haein.shoppingmall.dto.ReviewRequest;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.repository.ReviewCommentRepository;
import com.haein.shoppingmall.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ItemService itemService;
    private final MemberService memberService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewCommentRepository reviewCommentRepository,
            ItemService itemService,
            MemberService memberService
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.itemService = itemService;
        this.memberService = memberService;
    }

    @Transactional
    public void createReview(Long itemId, ReviewRequest request, Long memberId) {
        if (request.content() == null || request.content().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "리뷰 내용을 입력해 주세요");
        }
        Item item = itemService.findItemEntity(itemId);
        Member member = memberService.findCurrentMember(memberId);
        Review review = new Review(
                request.content(),
                request.rating() == null ? 5 : request.rating(),
                request.productOption(),
                item,
                member
        );
        review.replacePictures(request.imageUrls());
        reviewRepository.save(review);
    }

    @Transactional
    public void createComment(Long reviewId, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다"));
        ReviewComment reviewComment = new ReviewComment(comment, review);
        review.setComment(reviewComment);
        reviewCommentRepository.save(reviewComment);
    }
}
