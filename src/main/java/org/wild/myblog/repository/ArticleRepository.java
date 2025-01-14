package org.wild.myblog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.wild.myblog.model.Article;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findArticlesByContentContaining(String content);
    List<Article> findArticlesByCreatedAtAfter(LocalDateTime createdAt);
    List<Article> findTop5ArticlesByOrderByCreatedAtDesc();
}