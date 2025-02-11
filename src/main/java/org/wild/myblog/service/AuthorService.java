package org.wild.myblog.service;

import org.springframework.stereotype.Service;
import org.wild.myblog.dto.AuthorDTO;
import org.wild.myblog.exception.ResourceNotFoundException;
import org.wild.myblog.mapper.AuthorMapper;
import org.wild.myblog.model.Article;
import org.wild.myblog.model.ArticleAuthor;
import org.wild.myblog.model.Author;
import org.wild.myblog.repository.ArticleAuthorRepository;
import org.wild.myblog.repository.ArticleRepository;
import org.wild.myblog.repository.AuthorRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final ArticleRepository articleRepository;
    private final ArticleAuthorRepository articleAuthorRepository;

    public AuthorService(
            AuthorRepository authorRepository,
            AuthorMapper authorMapper,
            ArticleRepository articleRepository,
            ArticleAuthorRepository articleAuthorRepository
    ) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
        this.articleRepository = articleRepository;
        this.articleAuthorRepository = articleAuthorRepository;
    }

    public List<AuthorDTO> getAllAuthors() {
        List<Author> authors = authorRepository.findAll();
        return authors.stream().map(authorMapper::convertToDTO).toList();
    }

    public AuthorDTO getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("L'auteur avec l'id " + id + " n'a pas été trouvé"));
        return authorMapper.convertToDTO(author);
    }

    public AuthorDTO createAuthor(Author author) {
        if (author.getArticleAuthors() != null) {
            for (ArticleAuthor articleAuthor : author.getArticleAuthors()) {
                Article article = articleAuthor.getArticle();
                final Long articleId = article.getId();
                article = articleRepository.findById(article.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("L'article à l'id " + articleId + " n'a pas été trouvé"));

                articleAuthor.setArticle(article);
                articleAuthor.setAuthor(author);

                articleAuthorRepository.save(articleAuthor);
            }
        }

        Author savedAuthor = authorRepository.save(author);
        return authorMapper.convertToDTO(savedAuthor);
    }

    public AuthorDTO updateAuthor(Long id, Author authorDetails) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("L'auteur à l'id " + id + " n'a pas été trouvé"));

        author.setFirstname(authorDetails.getFirstname());
        author.setLastname(authorDetails.getLastname());

        if (authorDetails.getArticleAuthors() != null) {
            // Supprimer les anciens ArticleAuthor
            for (ArticleAuthor oldArticleAuthor : author.getArticleAuthors()) {
                articleAuthorRepository.delete(oldArticleAuthor);
            }
            // équivalent sans boucle for : articleAuthorRepository.deleteAll(author.getArticleAuthors());

            List<ArticleAuthor> updatedArticleAuthors = new ArrayList<>();

            for (ArticleAuthor articleAuthorDetails : authorDetails.getArticleAuthors()) {
                Article article = articleAuthorDetails.getArticle();
                final Long articleId = article.getId();
                article = articleRepository.findById(article.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("L'article à l'id " + articleId + "n'a pas été trouvé"));

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
            // équivalent sans boucle : articleAuthorRepository.saveAll(updatedArticleAuthors);

            author.setArticleAuthors(updatedArticleAuthors);
        }

        Author updatedAuthor = authorRepository.save(author);
        return authorMapper.convertToDTO(updatedAuthor);
    }

    public boolean deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("L'auteur à l'id " + id + " n'a pas été trouvé"));
        if (author.getArticleAuthors() != null) {
            articleAuthorRepository.deleteAll(author.getArticleAuthors());
        }
        authorRepository.delete(author);
        return true;
    }
}
