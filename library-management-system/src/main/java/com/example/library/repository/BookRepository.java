package com.example.library.repository;

import com.example.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Find a book by ISBN
    Optional<Book> findByIsbn(String isbn);

    // Check if a book exists by ISBN
    boolean existsByIsbn(String isbn);

    // Delete a book by ISBN
    void deleteByIsbn(String isbn);
}
