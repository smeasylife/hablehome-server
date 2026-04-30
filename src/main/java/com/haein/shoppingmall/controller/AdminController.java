package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.dto.AdminAnswerRequest;
import com.haein.shoppingmall.dto.AdminQuestionResponse;
import com.haein.shoppingmall.dto.AdminReviewResponse;
import com.haein.shoppingmall.service.QuestionService;
import com.haein.shoppingmall.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-api")
public class AdminController {

    private final QuestionService questionService;
    private final ReviewService reviewService;

    public AdminController(QuestionService questionService, ReviewService reviewService) {
        this.questionService = questionService;
        this.reviewService = reviewService;
    }

    @GetMapping("/questions")
    public List<AdminQuestionResponse> findQuestions() {
        return questionService.findAdminQuestions();
    }

    @PutMapping("/questions/{questionId}/answer")
    public ResponseEntity<Void> updateQuestionAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody AdminAnswerRequest request
    ) {
        questionService.updateAnswer(questionId, request.content());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/questions/{questionId}/answer")
    public ResponseEntity<Void> deleteQuestionAnswer(@PathVariable Long questionId) {
        questionService.deleteAnswer(questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews")
    public List<AdminReviewResponse> findReviews() {
        return reviewService.findAdminReviews();
    }

    @PutMapping("/reviews/{reviewId}/comment")
    public ResponseEntity<Void> updateReviewComment(
            @PathVariable Long reviewId,
            @Valid @RequestBody AdminAnswerRequest request
    ) {
        reviewService.updateComment(reviewId, request.content());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/{reviewId}/comment")
    public ResponseEntity<Void> deleteReviewComment(@PathVariable Long reviewId) {
        reviewService.deleteComment(reviewId);
        return ResponseEntity.noContent().build();
    }
}
