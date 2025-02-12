package org.wild.myblog.service;

import org.springframework.stereotype.Service;
import org.wild.myblog.dto.article.ArticleCreateDTO;
import org.wild.myblog.dto.article.ArticleDTO;
import org.wild.myblog.dto.articleAuthor.AuthorContributionDTO;
import org.wild.myblog.dto.image.ImageDTO;
import org.wild.myblog.exception.ResourceNotFoundException;
import org.wild.myblog.mapper.ArticleMapper;
import org.wild.myblog.mapper.ImageMapper;
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
    private final ImageMapper imageMapper;

    public ArticleService(
            ArticleMapper articleMapper,
            ArticleRepository articleRepository,
            CategoryRepository categoryRepository,
            ImageRepository imageRepository,
            AuthorRepository authorRepository,
            ArticleAuthorRepository articleAuthorRepository,
            ImageMapper imageMapper
    ) {
        this.articleMapper = articleMapper;
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.authorRepository = authorRepository;
        this.articleAuthorRepository = articleAuthorRepository;
        this.imageMapper = imageMapper;
    }

    public List<ArticleDTO> getAllArticles() {
        List<Article> articles = articleRepository.findAll();
        return articles.stream().map(articleMapper::convertToDTO).toList();
    }

    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("L'article avec l'id " + id + " n'a pas été trouvé")
        );
        return articleMapper.convertToDTO(article);
    }

    public ArticleDTO createArticle(ArticleCreateDTO articleCreateDTO) {
        Article article = articleMapper.convertToEntity(articleCreateDTO);

        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());

        if (articleCreateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(articleCreateDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("La catégorie avec l'id " + articleCreateDTO.getCategoryId() + " n'a pas été trouvée"));
            article.setCategory(category);
        }

        if (articleCreateDTO.getImages() != null && !articleCreateDTO.getImages().isEmpty()) {
            List<Image> validImages = new ArrayList<>();
            for (ImageDTO imageDTO : articleCreateDTO.getImages()) {
                if (imageDTO.getId() != null) {
                    Image existingImage = imageRepository.findById(imageDTO.getId()).orElse(null);
                    if (existingImage != null) {
                        validImages.add(existingImage);
                    } else {
                        throw new ResourceNotFoundException("L'image avec l'id " + imageDTO.getId() + " n'a pas été trouvée");
                    }
                } else { // Si l'image n'existe pas en BDD, on la crée en BDD et on l'ajoute à la liste aussi
                    Image savedImage = imageRepository.save(imageMapper.convertToEntity(imageDTO));
                    validImages.add(savedImage);
                }
            }
            article.setImages(validImages);
        }

        Article savedArticle = articleRepository.save(article);

        if (articleCreateDTO.getAuthors()!= null) {
            List<ArticleAuthor> articleAuthors = new ArrayList<>();
            for (AuthorContributionDTO authorContributionDTO : articleCreateDTO.getAuthors()) {
                final Long authorId = authorContributionDTO.getAuthorId();
                Author author = authorRepository.findById(authorId)
                        .orElseThrow(() -> new ResourceNotFoundException("L'auteur à l'id " + authorId + " n'a pas été trouvé"));

                ArticleAuthor articleAuthor = new ArticleAuthor();
                articleAuthor.setAuthor(author);
                articleAuthor.setArticle(savedArticle);
                articleAuthor.setContribution(authorContributionDTO.getContribution());
                articleAuthors.add(articleAuthor);
            }
            articleAuthorRepository.saveAll(articleAuthors);
            savedArticle.setArticleAuthors(articleAuthors);
        }

        return articleMapper.convertToDTO(savedArticle);
    }

    public ArticleDTO updateArticle(
            Long id,
            Article articleDetails
    ) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("L'article avec l'id " + id + " n'a pas été trouvé"));

        article.setTitle(articleDetails.getTitle());
        article.setContent(articleDetails.getContent());
        article.setUpdatedAt(LocalDateTime.now());

        if (articleDetails.getCategory() != null) {
            Category category = categoryRepository.findById(articleDetails.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("La catégorie avec l'id " + articleDetails.getCategory().getId() + " n'a pas été trouvée"));
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
                        throw new ResourceNotFoundException("L'image avec l'id " + image.getId() + " n'a pas été trouvée");
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
                final Long authorId = author.getId();
                author = authorRepository.findById(author.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("L'auteur à l'id " + authorId + " n'a pas été trouvé"));

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
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("L'article avec l'id " + id + " n'a pas été trouvé"));
        articleAuthorRepository.deleteAll(article.getArticleAuthors());
        articleRepository.delete(article);
        return true;
    }
}
