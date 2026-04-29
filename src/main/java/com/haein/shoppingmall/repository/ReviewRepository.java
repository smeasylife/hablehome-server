package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByItemIdOrderByCreatedAtDesc(Long itemId);
}
