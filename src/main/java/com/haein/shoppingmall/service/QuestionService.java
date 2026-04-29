package com.haein.shoppingmall.service;

import com.haein.shoppingmall.domain.Item;
import com.haein.shoppingmall.domain.Member;
import com.haein.shoppingmall.domain.Question;
import com.haein.shoppingmall.dto.QuestionRequest;
import com.haein.shoppingmall.exception.BusinessException;
import com.haein.shoppingmall.repository.QuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ItemService itemService;
    private final MemberService memberService;

    public QuestionService(QuestionRepository questionRepository, ItemService itemService, MemberService memberService) {
        this.questionRepository = questionRepository;
        this.itemService = itemService;
        this.memberService = memberService;
    }

    @Transactional
    public void createQuestion(QuestionRequest request, Long memberId) {
        Item item = request.itemId() == null ? null : itemService.findItemEntity(request.itemId());
        Member member = memberService.findCurrentMember(memberId);
        questionRepository.save(new Question(request.title(), request.content(), item, member));
    }

    @Transactional
    public void answer(Long questionId, String answer) {
        if (answer == null || answer.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "답변 내용을 입력해 주세요");
        }
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다"));
        question.answer(answer);
    }
}
