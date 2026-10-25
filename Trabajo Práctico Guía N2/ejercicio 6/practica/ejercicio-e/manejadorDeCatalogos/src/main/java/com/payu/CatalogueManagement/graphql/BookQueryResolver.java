package com.payu.CatalogueManagement.graphql;

import com.payu.CatalogueManagement.entity.Book;
import com.payu.CatalogueManagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class BookQueryResolver {

    private final BookService bookService;

    @Autowired
    public BookQueryResolver(BookService bookService) {
        this.bookService = bookService;
    }

    @QueryMapping
    public List<Book> getAllBooks() {
        return bookService.findAll();
    }

}
