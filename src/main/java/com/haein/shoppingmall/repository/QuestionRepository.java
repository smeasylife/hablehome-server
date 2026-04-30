package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.Question;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByItemIdOrderByCreatedAtDesc(Long itemId);

    @EntityGraph(attributePaths = {"item", "member"})
    List<Question> findAllByOrderByCreatedAtDesc();
}
