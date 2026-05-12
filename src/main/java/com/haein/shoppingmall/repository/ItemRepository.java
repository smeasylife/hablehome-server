package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.Item;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @EntityGraph(attributePaths = "pictures")
    Optional<Item> findWithPicturesById(Long id);

    List<Item> findByNameContainingIgnoreCase(String keyword, Sort sort);
}
