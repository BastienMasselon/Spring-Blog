package org.wild.myblog.mapper;

import org.springframework.stereotype.Component;
import org.wild.myblog.dto.article.ArticleCreateDTO;
import org.wild.myblog.dto.article.ArticleDTO;
import org.wild.myblog.dto.author.AuthorDTO;
import org.wild.myblog.model.Article;
import org.wild.myblog.model.Image;
import org.wild.myblog.repository.CategoryRepository;

import java.util.stream.Collectors;

@Component
public class ArticleMapper {

    private final CategoryRepository categoryRepository;

    public ArticleMapper(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ArticleDTO convertToDTO(Article article) {
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setId(article.getId());
        articleDTO.setTitle(article.getTitle());
        articleDTO.setContent(article.getContent());
        articleDTO.setUpdatedAt(article.getUpdatedAt());

        if (article.getCategory() != null) {
            articleDTO.setCategoryName(article.getCategory().getName());
        }
        if (article.getImages() != null) {
            articleDTO.setImageUrls(article.getImages().stream().map(Image::getUrl).toList());
        }
        if (article.getArticleAuthors() != null) {
            articleDTO.setAuthors(article.getArticleAuthors().stream()
                .filter(articleAuthor -> articleAuthor.getAuthor() != null)
                .map(articleAuthor -> {
                    AuthorDTO authorDTO = new AuthorDTO();
                    authorDTO.setId(articleAuthor.getAuthor().getId());
                    authorDTO.setFirstname(articleAuthor.getAuthor().getFirstname());
                    authorDTO.setLastname(articleAuthor.getAuthor().getLastname());
                    authorDTO.setArticleIds(articleAuthor.getAuthor().getArticleAuthors().stream()
                            .filter(authorsArticleAuthor -> authorsArticleAuthor.getArticle() != null )
                            .map(authorsArticleAuthor -> authorsArticleAuthor.getArticle().getId())
                            .toList());
                    return authorDTO;
                })
                .collect(Collectors.toList())
            );
        }

        return articleDTO;
    }

    public Article convertToEntity(ArticleCreateDTO articleDTO) {
        Article article = new Article();
        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        return article;
    }
}
