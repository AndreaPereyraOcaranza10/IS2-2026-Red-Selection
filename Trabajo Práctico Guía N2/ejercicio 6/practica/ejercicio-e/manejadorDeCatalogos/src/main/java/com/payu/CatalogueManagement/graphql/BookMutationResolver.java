package com.payu.CatalogueManagement.graphql;

import com.payu.CatalogueManagement.entity.Book;
import com.payu.CatalogueManagement.entity.BookType;
import com.payu.CatalogueManagement.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
public class BookMutationResolver {

    private final BookService bookService;

    @Autowired
    public BookMutationResolver(BookService bookService) {
        this.bookService = bookService;
    }

    @MutationMapping
    public Book addBook(
            @Argument String name,
            @Argument String isbnNumber,
            @Argument String publishDate,
            @Argument Double price,
            @Argument String bookType
    ) {
        Book book = Book.builder()
                .name(name)
                .isbnNumber(isbnNumber)
                .publishDate(LocalDate.parse(publishDate))
                .price(price)
                .bookType(BookType.valueOf(bookType.toUpperCase()))
                .build();
        return bookService.save(book);
    }

    @MutationMapping
    public Book updateBook(
            @Argument Long id,
            @Argument String name,
            @Argument String isbnNumber,
            @Argument String publishDate,
            @Argument Double price,
            @Argument String bookType
    ) {
        Book book = Book.builder()
                .id(id)
                .name(name)
                .isbnNumber(isbnNumber)
                .publishDate(LocalDate.parse(publishDate))
                .price(price)
                .bookType(BookType.valueOf(bookType.toUpperCase()))
                .build();
        return bookService.update(id, book);
    }

    @MutationMapping
    public Boolean deleteBook(@Argument Long id) {
        bookService.delete(id);
        return true;
    }
}
