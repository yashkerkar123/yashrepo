package com.example.library.controller;

import com.example.library.entity.Book;
import com.example.library.entity.User;
import com.example.library.exception.BookNotFoundException;
import com.example.library.exception.UserNotFoundException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    // Add Book (Preventing Duplicate ISBNs)
    @PostMapping
    public ResponseEntity<?> addBook(@Valid @RequestBody Book book) {
        try {
            Optional<Book> existingBook = bookRepository.findByIsbn(book.getIsbn());
            if (existingBook.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Book with ISBN " + book.getIsbn() + " already exists.");
            }

            Book savedBook = bookRepository.save(book);
            return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error adding book: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding book");
        }
    }

    // Get All Books
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        try {
            List<Book> books = bookRepository.findAll();
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving books: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving books");
        }
    }

    // Get Book By ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        try {
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> new BookNotFoundException("Book not found with id " + id));
            return ResponseEntity.ok(book);
        } catch (Exception e) {
            logger.error("Error retrieving book by ID: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving book by ID");
        }
    }

    // Delete Book
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with id " + id);
        }
        bookRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Book deleted successfully");
    }


    // Assign Book to a Single User
    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignBookToUser(@PathVariable Long id, @RequestBody User user) {
        // Check if the user ID is missing
        if (user.getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("ID required");
        }

        try {
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> new BookNotFoundException("Book not found with id " + id));

            if (book.isBorrowed()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Book is already borrowed");
            }

            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id " + user.getId()));

            book.getUsers().add(existingUser);
            existingUser.getBorrowedBooks().add(book);
            book.setBorrowed(true);
            Book updatedBook = bookRepository.save(book);
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        } catch (BookNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error assigning book to user");
        }
    }


    // Assign Book to Multiple Users
    @PostMapping("/{id}/assigns")
    public ResponseEntity<Book> assignBookToUsers(@PathVariable Long id, @RequestBody List<User> users) {
        try {
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> new BookNotFoundException("Book not found with id " + id));

            if (book.isBorrowed()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
            }

            for (User user : users) {
                User existingUser = userRepository.findById(user.getId())
                        .orElseThrow(() -> new UserNotFoundException("User not found with id " + user.getId()));
                book.getUsers().add(existingUser);
                existingUser.getBorrowedBooks().add(book);
            }

            book.setBorrowed(true);
            Book updatedBook = bookRepository.save(book);
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error assigning book to users: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error assigning book to users");
        }
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Book> returnBook(@PathVariable Long id) {
        try {
            // Fetch the book by ID
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> new BookNotFoundException("Book not found with id " + id));

            // Check if the book is already returned
            if (!book.isBorrowed()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
            }

            // Unassign book from all users and mark as returned
            book.setBorrowed(false);
            book.getUsers().clear();  // Clear the list of users

            Book updatedBook = bookRepository.save(book);
            return new ResponseEntity<>(updatedBook, HttpStatus.OK);
        } catch (BookNotFoundException e) {
            // Log the specific exception
            logger.error("Book not found error: ", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            // Log unexpected errors
            logger.error("Unexpected error occurred: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error returning book");
        }
    }
}
