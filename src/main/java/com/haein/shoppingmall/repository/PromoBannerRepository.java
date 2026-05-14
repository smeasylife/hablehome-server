package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.PromoBanner;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromoBannerRepository extends JpaRepository<PromoBanner, Long> {

    @EntityGraph(attributePaths = "targetItem")
    List<PromoBanner> findAllByOrderByDisplayOrderAscIdAsc();

    @Modifying
    @Query("update PromoBanner banner set banner.targetItem = null where banner.targetItem.id = :itemId")
    void clearTargetItemByItemId(@Param("itemId") Long itemId);
}
