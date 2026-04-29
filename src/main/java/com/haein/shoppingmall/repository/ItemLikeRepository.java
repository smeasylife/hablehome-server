package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.ItemLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemLikeRepository extends JpaRepository<ItemLike, Long> {

    boolean existsByItemIdAndMemberId(Long itemId, Long memberId);
}
