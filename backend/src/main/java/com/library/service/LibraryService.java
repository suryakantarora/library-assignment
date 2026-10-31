package com.library.service;

import com.library.dto.BookDetail;
import com.library.dto.BookView;
import com.library.dto.StatusResponse;
import com.library.dto.WaitlistPosition;
import com.library.exception.ForbiddenException;
import com.library.exception.NotFoundException;
import com.library.model.Book;
import com.library.model.User;
import com.library.outcome.AddBookOutcome;
import com.library.outcome.BorrowOutcome;
import com.library.outcome.ReturnOutcome;
import com.library.outcome.WaitlistOutcome;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LibraryService {

    private final BookRepository books;
    private final UserRepository users;

    public LibraryService(BookRepository books, UserRepository users) {
        this.books = books;
        this.users = users;
    }

    public synchronized User login(String name) {
        String trimmed = name.trim();
        return users.findOrCreate(trimmed.equalsIgnoreCase(User.ADMIN) ? User.ADMIN : trimmed);
    }

    public synchronized List<BookView> listBooks(User user) {
        return books.findAll().stream()
                .map(book -> new BookView(
                        book.getTitle(),
                        book.isAvailable(),
                        book.isBorrowedBy(user),
                        book.positionOf(user).orElse(null)))
                .toList();
    }

    public synchronized BookDetail bookDetail(User user, String title) {
        Book book = books.findByTitle(title)
                .orElseThrow(() -> new NotFoundException("Sorry, \"%s\" is not registered.".formatted(title.trim())));
        String borrowedBy = user.isAdmin()
                ? book.borrowerKey().flatMap(users::findByName).map(User::getName).orElse(null)
                : null;
        return new BookDetail(
                book.getTitle(),
                book.isAvailable(),
                book.isBorrowedBy(user),
                borrowedBy,
                book.waitlistSize(),
                book.positionOf(user).orElse(null));
    }

    public synchronized AddBookOutcome addBook(User user, String title) {
        if (!user.isAdmin()) {
            throw new ForbiddenException("Only admin can add books to the library.");
        }
        String cleaned = title.trim();
        if (books.findByTitle(cleaned).isPresent()) {
            return new AddBookOutcome.AlreadyExists(cleaned);
        }
        books.save(new Book(cleaned));
        return new AddBookOutcome.Added(cleaned);
    }

    public synchronized BorrowOutcome borrow(User user, String title) {
        requireMember(user);
        Optional<Book> found = books.findByTitle(title);
        if (found.isEmpty()) {
            return new BorrowOutcome.NotRegistered(title.trim());
        }
        Book book = found.get();
        if (book.isBorrowedBy(user)) {
            return new BorrowOutcome.AlreadyBorrowed(book.getTitle());
        }
        if (!book.isAvailable()) {
            return new BorrowOutcome.Unavailable(book.getTitle());
        }
        book.lendTo(user);
        return new BorrowOutcome.Borrowed(book.getTitle());
    }

    public synchronized ReturnOutcome returnBook(User user, String title) {
        requireMember(user);
        Optional<Book> found = books.findByTitle(title);
        if (found.isEmpty()) {
            return new ReturnOutcome.NotRegistered(title.trim());
        }
        Book book = found.get();
        if (!book.isBorrowedBy(user)) {
            return new ReturnOutcome.NotBorrowed(book.getTitle());
        }
        book.release();
        passToNextInLine(book);
        return new ReturnOutcome.Returned(book.getTitle());
    }

    public synchronized WaitlistOutcome joinWaitlist(User user, String title) {
        requireMember(user);
        Optional<Book> found = books.findByTitle(title);
        if (found.isEmpty()) {
            return new WaitlistOutcome.NotRegistered(title.trim());
        }
        Book book = found.get();
        if (book.isBorrowedBy(user)) {
            return new WaitlistOutcome.AlreadyBorrowed(book.getTitle());
        }
        if (book.isAvailable()) {
            return new WaitlistOutcome.BookAvailable(book.getTitle());
        }
        Optional<Integer> existing = book.positionOf(user);
        if (existing.isPresent()) {
            return new WaitlistOutcome.AlreadyWaiting(book.getTitle(), existing.get());
        }
        book.enqueue(user);
        return new WaitlistOutcome.Added(book.getTitle(), book.positionOf(user).orElseThrow());
    }

    public synchronized StatusResponse status(User user) {
        List<String> borrowed = books.findAll().stream()
                .filter(book -> book.isBorrowedBy(user))
                .map(Book::getTitle)
                .toList();
        List<WaitlistPosition> waitlists = books.findAll().stream()
                .flatMap(book -> book.positionOf(user)
                        .map(position -> new WaitlistPosition(book.getTitle(), position))
                        .stream())
                .toList();
        return new StatusResponse(borrowed, waitlists);
    }

    public synchronized List<String> collectNotifications(User user) {
        return user.drainNotifications();
    }

    private void passToNextInLine(Book book) {
        book.pollNextInLine()
                .flatMap(users::findByName)
                .ifPresent(next -> {
                    book.lendTo(next);
                    next.notify("You got the book \"%s\" from your waitlist".formatted(book.getTitle()));
                });
    }

    private void requireMember(User user) {
        if (user.isAdmin()) {
            throw new ForbiddenException("Admin can only manage books, not borrow them.");
        }
    }
}
