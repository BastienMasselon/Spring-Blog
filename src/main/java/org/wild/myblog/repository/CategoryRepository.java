package org.wild.myblog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wild.myblog.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}