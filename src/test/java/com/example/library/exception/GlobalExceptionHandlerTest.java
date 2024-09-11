package com.example.library.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleBookNotFoundExceptionTest() {
        BookNotFoundException ex = new BookNotFoundException("Book not found with id 1");
        ResponseEntity<String> response = exceptionHandler.handleBookNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book not found with id 1", response.getBody());
    }

    @Test
    void handleUserNotFoundExceptionTest() {
        UserNotFoundException ex = new UserNotFoundException("User not found with id 1");
        ResponseEntity<String> response = exceptionHandler.handleUserNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found with id 1", response.getBody());
    }

    @Test
    void handleGenericExceptionTest() {
        Exception ex = new Exception("Generic error");
        ResponseEntity<String> response = exceptionHandler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred: Generic error", response.getBody());
    }
}
