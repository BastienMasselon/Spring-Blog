package org.wild.myblog.mapper;

import org.springframework.stereotype.Component;
import org.wild.myblog.dto.author.AuthorCreateDTO;
import org.wild.myblog.dto.author.AuthorDTO;
import org.wild.myblog.model.Author;

@Component
public class AuthorMapper {
    public AuthorDTO convertToDTO(Author author) {
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

    public Author convertToEntity(AuthorCreateDTO authorCreateDTO) {
        Author author = new Author();
        author.setFirstname(authorCreateDTO.getFirstname());
        author.setLastname(authorCreateDTO.getLastname());
        return author;
    }
}
