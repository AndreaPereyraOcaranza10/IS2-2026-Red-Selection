package com.payu.CatalogueManagement.service;

import com.payu.CatalogueManagement.entity.Book;
import com.payu.CatalogueManagement.entity.BookType;
import com.payu.CatalogueManagement.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class BookServiceTest {

    private BookRepository repository;
    private BookService service;

    @BeforeEach
    void setUp() {
        repository = mock(BookRepository.class);
        service = new BookService(repository);
    }

    @Test
    void testFindAll_ShouldReturnListOfBooks() {
        Book book1 = new Book(1L, "Book One", "123456", LocalDate.now(), 100.0, BookType.EBOOK);
        Book book2 = new Book(2L, "Book Two", "654321", LocalDate.now(), 150.0, BookType.HARDCOVER);
        when(repository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<Book> result = service.findAll();

        assertThat(result).hasSize(2).contains(book1, book2);
        verify(repository).findAll();
    }

    @Test
    void testSave_ShouldReturnSavedBook() {
        Book book = new Book(null, "New Book", "112233", LocalDate.now(), 120.0, BookType.SOFTCOVER);
        Book savedBook = new Book(1L, "New Book", "112233", LocalDate.now(), 120.0, BookType.SOFTCOVER);
        when(repository.save(book)).thenReturn(savedBook);

        Book result = service.save(book);

        assertThat(result).isEqualTo(savedBook);
        verify(repository).save(book);
    }

    @Test
    void testUpdate_ShouldUpdateAndReturnBook() {
        Book bookToUpdate = new Book(5L, "Old Book", "998877", LocalDate.now(), 180.0, BookType.EBOOK);
        Book updatedBook = new Book(5L, "Updated Book", "998877", LocalDate.now(), 185.0, BookType.EBOOK);
        when(repository.save(any(Book.class))).thenReturn(updatedBook);

        Book result = service.update(5L, bookToUpdate);

//        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Updated Book");
        verify(repository).save(bookToUpdate);
    }

    @Test
    void testDelete_ShouldCallRepositoryDeleteById() {
        Long idToDelete = 10L;

        service.delete(idToDelete);

        verify(repository).deleteById(idToDelete);
    }
}
