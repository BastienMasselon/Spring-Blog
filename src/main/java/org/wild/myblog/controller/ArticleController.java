package org.wild.myblog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wild.myblog.dto.ArticleDTO;
import org.wild.myblog.dto.AuthorDTO;
import org.wild.myblog.model.*;
import org.wild.myblog.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final AuthorRepository authorRepository;
    private final ArticleAuthorRepository articleAuthorRepository;

    public ArticleController(ArticleRepository articleRepository, CategoryRepository categoryRepository, ImageRepository imageRepository, AuthorRepository authorRepository, ArticleAuthorRepository articleAuthorRepository) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.authorRepository = authorRepository;
        this.articleAuthorRepository = articleAuthorRepository;
    }

    private ArticleDTO convertToDTO(Article article) {
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
                    return authorDTO;
                })
                .collect(Collectors.toList())
            );
        }
        return articleDTO;
    }

    @GetMapping
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        List<Article> articles = articleRepository.findAll();
        if (articles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<ArticleDTO> articleDTOs = articles.stream().map(this::convertToDTO).toList();
        return ResponseEntity.ok(articleDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToDTO(article));
    }

    @PostMapping
    public ResponseEntity<ArticleDTO> createArticle(@RequestBody Article article) {
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());

        if (article.getCategory() != null) {
            Category category = categoryRepository.findById(article.getCategory().getId()).orElse(null);
            if (category == null) {
                return ResponseEntity.badRequest().body(null);
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
                        return ResponseEntity.badRequest().body(null);
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
                author = authorRepository.findById(author.getId().orElse(null));
                if (author == null) {
                    return ResponseEntity.badRequest().body(null);
                }

                articleAuthor.setAuthor(author);
                articleAuthor.setArticle(savedArticle);
                articleAuthor.setContribution(articleAuthor.getContribution());

                articleAuthorRepository.save(articleAuthor);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedArticle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleDTO> updateArticle(
            @PathVariable Long id,
            @RequestBody Article articleDetails
    ) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }

        article.setTitle(articleDetails.getTitle());
        article.setContent(articleDetails.getContent());
        article.setUpdatedAt(LocalDateTime.now());

        if (articleDetails.getCategory() != null) {
            Category category = categoryRepository.findById(articleDetails.getCategory().getId()).orElse(null);
            if (category == null) {
                return ResponseEntity.badRequest().body(null);
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
                        return ResponseEntity.badRequest().build();
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

        Article updatedArticle = articleRepository.save(article);
        return ResponseEntity.ok(convertToDTO(updatedArticle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Article> deleteArticle(@PathVariable Long id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        articleRepository.delete(article);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search-content")
    public ResponseEntity<List<ArticleDTO>> getArticlesByContent(@RequestParam String searchTerms) {
        List<Article> articles = articleRepository.findArticlesByContentContaining(searchTerms);
        if (articles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<ArticleDTO> articleDTOs = articles.stream().map(this::convertToDTO).toList();
        return ResponseEntity.ok(articleDTOs);
    }

    @GetMapping("/search-after")
    public ResponseEntity<List<ArticleDTO>> getArticlesByCreationDateAfter(@RequestParam String searchTerms) {
        List<Article> articles = articleRepository.findArticlesByCreatedAtAfter(LocalDateTime.parse(searchTerms));
        if (articles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<ArticleDTO> articleDTOs = articles.stream().map(this::convertToDTO).toList();
        return ResponseEntity.ok(articleDTOs);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<ArticleDTO>> getFiveLastArticles() {
        List<Article> articles = articleRepository.findTop5ArticlesByOrderByCreatedAtDesc();
        if (articles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<ArticleDTO> articleDTOs = articles.stream().map(this::convertToDTO).toList();
        return ResponseEntity.ok(articleDTOs);
    }
}
