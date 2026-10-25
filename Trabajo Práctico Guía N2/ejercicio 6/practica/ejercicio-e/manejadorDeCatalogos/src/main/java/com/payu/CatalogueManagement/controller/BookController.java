package com.payu.CatalogueManagement.controller;

import com.payu.CatalogueManagement.entity.Book;
import com.payu.CatalogueManagement.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Book API", description = "CRUD Operations for Managing books")
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    @Autowired
    public BookController(BookService service) {
        this.service = service;
    }

    @Operation(summary = "Get all books", description = "Retrieve a list of all books in the catalogue.")
    @GetMapping("/getAllBooks")
    public List<Book> getAllBooks() {
        return service.findAll();
    }

    @Operation(summary = "Add a new book", description = "Create and save a new book to the catalogue.")
    @PostMapping("/addBook")
    public Book addBook(@RequestBody Book book) {
        return service.save(book);
    }

    @Operation(summary = "Update an existing book", description = "Update the details of an existing book by ID.")
    @PutMapping("/updateBook/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book book) {
        return service.update(id, book);
    }

    @Operation(summary = "Delete a book", description = "Delete a book from the catalogue by ID.")
    @DeleteMapping("/deleteBook/{id}")
    public void deleteBook(@PathVariable Long id) {
        service.delete(id);
    }

//    triggering pipeline
}
