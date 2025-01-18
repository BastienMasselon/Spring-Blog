package org.wild.myblog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.wild.myblog.model.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
