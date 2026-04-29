package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.Cart;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    boolean existsByItemIdAndMemberId(Long itemId, Long memberId);

    @EntityGraph(attributePaths = {"item", "item.pictures"})
    List<Cart> findByMemberIdOrderByIdDesc(Long memberId);

    void deleteByIdInAndMemberId(Collection<Long> ids, Long memberId);
}
