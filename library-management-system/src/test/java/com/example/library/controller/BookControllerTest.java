package com.example.library.controller;

import com.example.library.entity.Book;
import com.example.library.entity.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookControllerTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookController bookController;

    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        book = new Book();
        book.setId(1L);
        book.setTitle("Springboot");
        book.setAuthor("Yash kerkar");
        book.setIsbn("9090134685991");
        book.setPageCount(416);
        book.setPublishedDate("2024-01-11");
        book.setBorrowed(false); // Ensure this is set to false initially

        user = new User();
        user.setId(1L);
        user.setName("yash gawas");
        user.setEmail("yash.gawas@gmail.com");
    }

    @Test
    void testAddBook() {
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        ResponseEntity<Book> response = (ResponseEntity<Book>) bookController.addBook(book);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(book.getTitle(), Objects.requireNonNull(response.getBody()).getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testGetAllBooks() {
        when(bookRepository.findAll()).thenReturn(Collections.singletonList(book));

        ResponseEntity<List<Book>> response = bookController.getAllBooks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
    }

    @Test
    void testGetBookById() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        ResponseEntity<Book> response = bookController.getBookById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(book.getTitle(), Objects.requireNonNull(response.getBody()).getTitle());
    }

    @Test
    void testGetBookById_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            bookController.getBookById(1L);
        });
    }

    @Test
    void testDeleteBook_NotFound() {
        when(bookRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> {
            bookController.deleteBook(1L);
        });
    }


    @Test
    void testDeleteBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<String> response = bookController.deleteBook(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(bookRepository, times(1)).deleteById(1L);
    }


    @Test
    void testAssignBookToUsers() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Set<User> users = new HashSet<>();
        users.add(user);
        book.setUsers(users);

        ResponseEntity<Book> response = bookController.assignBookToUsers(1L, Collections.singletonList(user));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).getUsers().size());
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void testReturnBook() {
        // Setup
        book.setBorrowed(true); // Book should be marked as borrowed initially
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        ResponseEntity<Book> response = bookController.returnBook(1L);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isBorrowed()); // Ensure book is returned
    }
}
