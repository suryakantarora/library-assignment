package com.library.repository;

import com.library.model.Book;
import com.library.model.Keys;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryBookRepository implements BookRepository {

    private final Map<String, Book> books = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public Optional<Book> findByTitle(String title) {
        return Optional.ofNullable(books.get(Keys.of(title)));
    }

    @Override
    public List<Book> findAll() {
        synchronized (books) {
            return new ArrayList<>(books.values());
        }
    }

    @Override
    public void save(Book book) {
        books.put(book.key(), book);
    }
}
