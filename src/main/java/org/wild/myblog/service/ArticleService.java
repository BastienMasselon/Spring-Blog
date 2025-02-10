package org.wild.myblog.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.wild.myblog.dto.ArticleDTO;
import org.wild.myblog.mapper.ArticleMapper;
import org.wild.myblog.model.*;
import org.wild.myblog.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final AuthorRepository authorRepository;
    private final ArticleAuthorRepository articleAuthorRepository;

    public ArticleService(
            ArticleMapper articleMapper,
            ArticleRepository articleRepository,
            CategoryRepository categoryRepository,
            ImageRepository imageRepository,
            AuthorRepository authorRepository,
            ArticleAuthorRepository articleAuthorRepository) {
        this.articleMapper = articleMapper;
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.authorRepository = authorRepository;
        this.articleAuthorRepository = articleAuthorRepository;
    }

    public List<ArticleDTO> getAllArticles() {
        List<Article> articles = articleRepository.findAll();
        return articles.stream().map(articleMapper::convertToDTO).toList();
    }

    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            return null;
        }
        return articleMapper.convertToDTO(article);
    }

    public ArticleDTO createArticle(Article article) {
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());

        if (article.getCategory() != null) {
            Category category = categoryRepository.findById(article.getCategory().getId()).orElse(null);
            if (category == null) {
                return null;
            }
            article.setCategory(category);
        }

        if (article.getImages() != null && !article.getImages().isEmpty()) {
            List<Image> validImages = new ArrayList<>();
            for (Image image : article.getImages()) {
                if (image.getId() != null) {
                    Image existingImage = imageRepository.findById(image.getId()).orElse(null);
                    if (existingImage != null) {
                        validImages.add(existingImage);
                    } else {
                        return null;
                    }
                } else { // Si l'image n'existe pas en BDD, on la crée en BDD et on l'ajoute à la liste aussi
                    Image savedImage = imageRepository.save(image);
                    validImages.add(savedImage);
                }
            }
            article.setImages(validImages);
        }

        Article savedArticle = articleRepository.save(article);

        if (article.getArticleAuthors() != null) {
            for (ArticleAuthor articleAuthor : article.getArticleAuthors()) {
                Author author = articleAuthor.getAuthor();
                author = authorRepository.findById(author.getId()).orElse(null);
                if (author == null) {
                    return null;
                }

                articleAuthor.setAuthor(author);
                articleAuthor.setArticle(savedArticle);
                articleAuthor.setContribution(articleAuthor.getContribution());

                articleAuthorRepository.save(articleAuthor);
            }
        }

        return articleMapper.convertToDTO(article);
    }

    public ArticleDTO updateArticle(
            Long id,
            Article articleDetails
    ) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            return null;
        }

        article.setTitle(articleDetails.getTitle());
        article.setContent(articleDetails.getContent());
        article.setUpdatedAt(LocalDateTime.now());

        if (articleDetails.getCategory() != null) {
            Category category = categoryRepository.findById(articleDetails.getCategory().getId()).orElse(null);
            if (category == null) {
                return null;
            }
            article.setCategory(category);
        }

        if (articleDetails.getImages() != null && !articleDetails.getImages().isEmpty()) {
            List<Image> validImages = new ArrayList<>();
            for (Image image : articleDetails.getImages()) {
                if (image.getId() != null) {
                    Image existingImage = imageRepository.findById(image.getId()).orElse(null);
                    if (existingImage != null) {
                        validImages.add(existingImage);
                    } else {
                        return null;
                    }
                } else {
                    Image savedImage = imageRepository.save(image);
                    validImages.add(savedImage);
                }
            }
            article.setImages(validImages);
        } else {
            // Si aucune image n'est fournie, on vide la liste des images
            article.getImages().clear();
        }

        if (articleDetails.getArticleAuthors() != null) {
            for (ArticleAuthor oldArticleAuthor : article.getArticleAuthors()) {
                articleAuthorRepository.delete(oldArticleAuthor);
            }

            List<ArticleAuthor> updatedArticleAuthors = new ArrayList<>();

            for (ArticleAuthor articleAuthor : articleDetails.getArticleAuthors()) {
                Author author = articleAuthor.getAuthor();
                author = authorRepository.findById(author.getId()).orElse(null);
                if (author == null) {
                    return null;
                }

                ArticleAuthor newArticleAuthor = new ArticleAuthor();
                newArticleAuthor.setAuthor(author);
                newArticleAuthor.setArticle(article);
                newArticleAuthor.setContribution(articleAuthor.getContribution());

                updatedArticleAuthors.add(newArticleAuthor);
            }

            for (ArticleAuthor articleAuthor : updatedArticleAuthors) {
                articleAuthorRepository.save(articleAuthor);
            }

            article.setArticleAuthors(updatedArticleAuthors);
        }

        Article updatedArticle = articleRepository.save(article);
        return articleMapper.convertToDTO(updatedArticle);
    }

    public boolean deleteArticle(Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            return false;
        }
        articleAuthorRepository.deleteAll(article.getArticleAuthors());
        articleRepository.delete(article);
        return true;
    }
}
