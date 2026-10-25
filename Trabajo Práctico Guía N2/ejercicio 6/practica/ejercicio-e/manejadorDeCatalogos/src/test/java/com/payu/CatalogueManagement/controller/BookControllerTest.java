package com.payu.CatalogueManagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payu.CatalogueManagement.entity.Book;
import com.payu.CatalogueManagement.entity.BookType;
import com.payu.CatalogueManagement.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Book sampleBook;

    @BeforeEach
    void setup() {
        sampleBook = new Book(1L, "Test Book", "ISBN123", LocalDate.of(2023, 1, 1), 199.99, BookType.HARDCOVER);
    }

    @Test
    void testGetAllBooks_ShouldReturnListOfBooks() throws Exception {
        List<Book> books = Arrays.asList(sampleBook);
        Mockito.when(service.findAll()).thenReturn(books);

        mockMvc.perform(get("/api/books/getAllBooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Book"));
    }

    @Test
    void testAddBook_ShouldReturnCreatedBook() throws Exception {
        Mockito.when(service.save(any(Book.class))).thenReturn(sampleBook);

        mockMvc.perform(post("/api/books/addBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Book"))
                .andExpect(jsonPath("$.isbnNumber").value("ISBN123"));
    }

    @Test
    void testUpdateBook_ShouldReturnUpdatedBook() throws Exception {
        Mockito.when(service.update(eq(1L), any(Book.class))).thenReturn(sampleBook);

        mockMvc.perform(put("/api/books/updateBook/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Book"))
                .andExpect(jsonPath("$.bookType").value("HARDCOVER"));
    }

    @Test
    void testDeleteBook_ShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/books/deleteBook/1"))
                .andExpect(status().isOk());

        Mockito.verify(service).delete(1L);
    }
}

