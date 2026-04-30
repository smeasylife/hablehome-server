package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.Review;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByItemIdOrderByCreatedAtDesc(Long itemId);

    @EntityGraph(attributePaths = {"item", "member", "comment", "pictures"})
    List<Review> findAllByOrderByCreatedAtDesc();
}
