package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.ItemOption;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ItemOptionRepository extends JpaRepository<ItemOption, Long> {

    Optional<ItemOption> findByItemIdAndColorAndSize(Long itemId, String color, String size);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ItemOption> findWithLockByItemIdAndColorAndSize(Long itemId, String color, String size);
}
