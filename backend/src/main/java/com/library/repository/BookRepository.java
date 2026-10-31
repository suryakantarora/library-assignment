package com.library.repository;

import com.library.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Optional<Book> findByTitle(String title);

    List<Book> findAll();

    void save(Book book);
}
