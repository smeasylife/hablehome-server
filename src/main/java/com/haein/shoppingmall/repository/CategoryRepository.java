package com.haein.shoppingmall.repository;

import com.haein.shoppingmall.domain.Category;
import com.haein.shoppingmall.domain.CategoryName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(CategoryName name);

    boolean existsByName(CategoryName name);
}
