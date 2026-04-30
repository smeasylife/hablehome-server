package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.domain.Review;
import com.haein.shoppingmall.domain.ReviewComment;
import com.haein.shoppingmall.dto.AdminReviewResponse;
import com.haein.shoppingmall.dto.ReviewRequest;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.repository.ReviewCommentRepository;
import com.haein.shoppingmall.repository.ReviewRepository;
import java.util.List;
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
        if (comment == null || comment.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "답변 내용을 입력해 주세요");
        }
        Review review = findReview(reviewId);
        if (review.getComment() != null) {
            review.getComment().update(comment);
            return;
        }
        ReviewComment reviewComment = reviewCommentRepository.save(new ReviewComment(comment, review));
        review.setComment(reviewComment);
    }

    @Transactional(readOnly = true)
    public List<AdminReviewResponse> findAdminReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(review -> new AdminReviewResponse(
                        review.getId(),
                        review.getItem() == null ? null : review.getItem().getId(),
                        review.getItem() == null ? "상품 없음" : review.getItem().getName(),
                        review.getMember() == null ? "알 수 없음" : review.getMember().getNickname(),
                        review.getRating(),
                        review.getProductOption(),
                        review.getPictures().stream().map(picture -> picture.getUrl()).toList(),
                        review.getContent(),
                        review.getComment() == null ? null : review.getComment().getComment(),
                        review.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void updateComment(Long reviewId, String comment) {
        createComment(reviewId, comment);
    }

    @Transactional
    public void deleteComment(Long reviewId) {
        Review review = findReview(reviewId);
        ReviewComment comment = review.getComment();
        if (comment == null) {
            return;
        }
        review.removeComment();
        reviewCommentRepository.delete(comment);
    }

    private Review findReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다"));
    }
}
