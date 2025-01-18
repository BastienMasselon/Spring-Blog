package org.wild.myblog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wild.myblog.model.ArticleAuthor;

@Repository
public interface ArticleAuthorRepository extends JpaRepository<ArticleAuthor, Long> {
}
