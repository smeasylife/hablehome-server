package com.haein.shoppingmall.controller;

import com.haein.shoppingmall.dto.QuestionRequest;
import com.haein.shoppingmall.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/question")
    public ResponseEntity<Void> createQuestion(
            @Valid @RequestBody QuestionRequest request,
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId
    ) {
        questionService.createQuestion(request, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/answer/{questionId}")
    public ResponseEntity<Void> answer(@PathVariable Long questionId, @RequestParam String answer) {
        questionService.answer(questionId, answer);
        return ResponseEntity.ok().build();
    }
}
