package org.wild.myblog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wild.myblog.dto.AuthorDTO;
import org.wild.myblog.model.Article;
import org.wild.myblog.model.ArticleAuthor;
import org.wild.myblog.model.Author;
import org.wild.myblog.repository.ArticleAuthorRepository;
import org.wild.myblog.repository.ArticleRepository;
import org.wild.myblog.repository.AuthorRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
public class AuthorController {

    private final AuthorRepository authorRepository;
    private final ArticleRepository articleRepository;
    private final ArticleAuthorRepository articleAuthorRepository;

    public AuthorController(AuthorRepository authorRepository, ArticleRepository articleRepository, ArticleAuthorRepository articleAuthorRepository) {
        this.authorRepository = authorRepository;
        this.articleRepository = articleRepository;
        this.articleAuthorRepository = articleAuthorRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuthorDTO>> getAllAuthors() {
        List<Author> authors = authorRepository.findAll();
        if (authors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<AuthorDTO> authorDTOs = authors.stream().map(this::convertToDTO).toList();
        return ResponseEntity.ok(authorDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorDTO> getAuthorById(@PathVariable Long id) {
        Author author = authorRepository.findById(id).orElse(null);
        if (author == null) {
            return ResponseEntity.notFound().build();
        }
        AuthorDTO authorDTO = convertToDTO(author);
        return ResponseEntity.ok(authorDTO);
    }

    @PostMapping
    public ResponseEntity<AuthorDTO> createAuthor(@RequestBody Author author) {
        if (author.getArticleAuthors() != null) {
            for (ArticleAuthor articleAuthor : author.getArticleAuthors()) {
                Article article = articleAuthor.getArticle();
                article = articleRepository.findById(article.getId()).orElse(null);
                if (article == null) {
                    return ResponseEntity.badRequest().body(null);
                }

                articleAuthor.setArticle(article);
                articleAuthor.setAuthor(author);

                articleAuthorRepository.save(articleAuthor);
            }
        }

        Author savedAuthor = authorRepository.save(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedAuthor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorDTO> updateAuthor(@PathVariable Long id, @RequestBody Author authorDetails) {
        Author author = authorRepository.findById(id).orElse(null);
        if (author == null) {
            return ResponseEntity.notFound().build();
        }

        author.setFirstname(authorDetails.getFirstname());
        author.setLastname(authorDetails.getLastname());

        if (authorDetails.getArticleAuthors() != null) {
            // Supprimer les anciens ArticleAuthor
            for (ArticleAuthor oldArticleAuthor : author.getArticleAuthors()) {
                articleAuthorRepository.delete(oldArticleAuthor);
            }

            List<ArticleAuthor> updatedArticleAuthors = new ArrayList<>();

            for (ArticleAuthor articleAuthorDetails : authorDetails.getArticleAuthors()) {
                Article article = articleAuthorDetails.getArticle();
                article = articleRepository.findById(article.getId()).orElse(null);
                if (article == null) {
                    return ResponseEntity.badRequest().build();
                }

                // Créer et associer la nouvelle relation ArticleAuthor
                ArticleAuthor newArticleAuthor = new ArticleAuthor();
                newArticleAuthor.setArticle(article);
                newArticleAuthor.setAuthor(author);
                newArticleAuthor.setContribution(articleAuthorDetails.getContribution());

                updatedArticleAuthors.add(newArticleAuthor);
            }

            for (ArticleAuthor articleAuthor : updatedArticleAuthors) {
                articleAuthorRepository.save(articleAuthor);
            }

            author.setArticleAuthors(updatedArticleAuthors);
        }
    }

    private AuthorDTO convertToDTO(Author author) {
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setId(author.getId());
        authorDTO.setFirstname(author.getFirstname());
        authorDTO.setLastname(author.getLastname());
        if (author.getArticleAuthors() != null) {
            authorDTO.setArticleIds(author.getArticleAuthors().stream()
                    .filter(articleAuthor -> articleAuthor.getArticle() != null)
                    .map( articleAuthor -> {
                        return articleAuthor.getArticle().getId();
                    })
                    .toList());
        }
        return authorDTO;
    }
}
