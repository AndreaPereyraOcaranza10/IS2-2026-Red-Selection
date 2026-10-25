package com.payu.CatalogueManagement.service;

import com.payu.CatalogueManagement.entity.Book;
import com.payu.CatalogueManagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;

    @Autowired
    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll() {
        return repository.findAll();
    }

    public Book save(Book book) {
        return repository.save(book);
    }

    public Book update(Long id, Book book) {
        book.setId(id);
        return repository.save(book);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
