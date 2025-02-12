package org.wild.myblog.dto.articleAuthor;

import org.wild.myblog.dto.article.ArticleDTO;
import org.wild.myblog.dto.author.AuthorDTO;

public class ArticleAuthorDTO {

    private Long id;
    private ArticleDTO article;
    private AuthorDTO author;
    private String contribution;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ArticleDTO getArticle() {
        return article;
    }

    public void setArticle(ArticleDTO article) {
        this.article = article;
    }

    public AuthorDTO getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDTO author) {
        this.author = author;
    }

    public String getContribution() {
        return contribution;
    }

    public void setContribution(String contribution) {
        this.contribution = contribution;
    }
}
