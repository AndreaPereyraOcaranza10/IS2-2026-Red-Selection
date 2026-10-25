package com.payu.CatalogueManagement.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder // <== this is required for Book.builder() to work
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(hidden = true)
    private Long id;

    private String name;
    private String isbnNumber;
    private LocalDate publishDate;
    private Double price;

    @Enumerated(EnumType.STRING)
    private BookType bookType;

    public Book(){

    }
    public Book(Long id, String name, String isbnNumber, LocalDate now, double price, BookType ebook) {
        this.id = id;
        this.name = name;
        this.isbnNumber = isbnNumber;
        this.publishDate = now;
        this.price = price;
        this.bookType = ebook;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsbnNumber() {
        return isbnNumber;
    }

    public void setIsbnNumber(String isbnNumber) {
        this.isbnNumber = isbnNumber;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public BookType getBookType() {
        return bookType;
    }

    public void setBookType(BookType bookType) {
        this.bookType = bookType;
    }
}

/*
package com.payu.CatalogueManagement.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(hidden = true)
    private Long id;

    private String name;
    private String isbnNumber;
    private LocalDate publishDate;
    private Double price;

    @Enumerated(EnumType.STRING)
    private BookType bookType;
}
 */

