package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.OrderStatus;
import com.haein.shoppingmall.domain.PurchaseOrder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
    List<PurchaseOrder> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.item"})
    Optional<PurchaseOrder> findByIdAndMemberId(Long id, Long memberId);

    boolean existsByMemberIdAndOrderItemsItemIdAndStatusIn(
            Long memberId,
            Long itemId,
            Collection<OrderStatus> statuses
    );
}
