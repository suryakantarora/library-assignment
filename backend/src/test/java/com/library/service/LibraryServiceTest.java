package com.library.service;

import com.library.dto.BookDetail;
import com.library.dto.StatusResponse;
import com.library.dto.WaitlistPosition;
import com.library.exception.ForbiddenException;
import com.library.exception.NotFoundException;
import com.library.model.User;
import com.library.outcome.AddBookOutcome;
import com.library.outcome.BorrowOutcome;
import com.library.outcome.ReturnOutcome;
import com.library.outcome.WaitlistOutcome;
import com.library.repository.InMemoryBookRepository;
import com.library.repository.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryServiceTest {

    private LibraryService library;
    private User admin;
    private User alice;
    private User bob;
    private User carol;

    @BeforeEach
    void setUp() {
        library = new LibraryService(new InMemoryBookRepository(), new InMemoryUserRepository());
        admin = library.login("admin");
        alice = library.login("Alice");
        bob = library.login("Bob");
        carol = library.login("Carol");
        library.addBook(admin, "The Great Gatsby");
        library.addBook(admin, "1984");
    }

    @Test
    void loginCreatesUserOnlyOnce() {
        assertSame(alice, library.login("Alice"));
        assertSame(alice, library.login("  alice "));
    }

    @Test
    void adminNameIsMatchedIgnoringCase() {
        User upper = library.login("ADMIN");
        assertTrue(upper.isAdmin());
        assertEquals("admin", upper.getName());
    }

    @Test
    void onlyAdminCanAddBooks() {
        assertThrows(ForbiddenException.class, () -> library.addBook(alice, "Dune"));
    }

    @Test
    void duplicateTitleIsRejectedIgnoringCase() {
        assertInstanceOf(AddBookOutcome.AlreadyExists.class, library.addBook(admin, "the great gatsby"));
        assertEquals(2, library.listBooks(alice).size());
    }

    @Test
    void adminCannotBorrow() {
        assertThrows(ForbiddenException.class, () -> library.borrow(admin, "1984"));
    }

    @Test
    void borrowedBookShowsAsUnavailable() {
        assertEquals(new BorrowOutcome.Borrowed("1984"), library.borrow(alice, "1984"));

        assertEquals(new BorrowOutcome.Unavailable("1984"), library.borrow(bob, "1984"));
        assertEquals(false, library.listBooks(bob).get(1).available());
        assertEquals(true, library.listBooks(alice).get(1).borrowedByMe());
    }

    @Test
    void borrowingUnknownBookIsReported() {
        assertEquals(new BorrowOutcome.NotRegistered("1985"), library.borrow(alice, "1985"));
    }

    @Test
    void borrowingSameBookTwiceIsRejected() {
        library.borrow(alice, "1984");
        assertEquals(new BorrowOutcome.AlreadyBorrowed("1984"), library.borrow(alice, "1984"));
    }

    @Test
    void titlesAreMatchedIgnoringCaseButKeepOriginalSpelling() {
        assertEquals(new BorrowOutcome.Borrowed("The Great Gatsby"), library.borrow(alice, "  the great GATSBY "));
    }

    @Test
    void waitlistPositionsFollowJoiningOrder() {
        library.borrow(alice, "1984");

        assertEquals(new WaitlistOutcome.Added("1984", 1), library.joinWaitlist(bob, "1984"));
        assertEquals(new WaitlistOutcome.Added("1984", 2), library.joinWaitlist(carol, "1984"));
    }

    @Test
    void waitlistRejectsUnknownAvailableOwnedAndRepeatedRequests() {
        library.borrow(alice, "1984");
        library.joinWaitlist(bob, "1984");

        assertEquals(new WaitlistOutcome.NotRegistered("1985"), library.joinWaitlist(bob, "1985"));
        assertEquals(new WaitlistOutcome.BookAvailable("The Great Gatsby"), library.joinWaitlist(bob, "The Great Gatsby"));
        assertEquals(new WaitlistOutcome.AlreadyBorrowed("1984"), library.joinWaitlist(alice, "1984"));
        assertEquals(new WaitlistOutcome.AlreadyWaiting("1984", 1), library.joinWaitlist(bob, "1984"));
    }

    @Test
    void returnMakesBookAvailableWhenNobodyIsWaiting() {
        library.borrow(alice, "1984");

        assertEquals(new ReturnOutcome.Returned("1984"), library.returnBook(alice, "1984"));
        assertTrue(library.listBooks(bob).get(1).available());
    }

    @Test
    void returnHandsBookToFirstInLineAndLeavesNotification() {
        library.borrow(alice, "1984");
        library.joinWaitlist(bob, "1984");
        library.joinWaitlist(carol, "1984");

        library.returnBook(alice, "1984");

        assertEquals(List.of("1984"), library.status(bob).borrowed());
        assertEquals(List.of("You got the book \"1984\" from your waitlist"), library.collectNotifications(bob));
        assertEquals(List.of(), library.collectNotifications(bob));
        assertEquals(new StatusResponse(List.of(), List.of(new WaitlistPosition("1984", 1))), library.status(carol));
        assertEquals(false, library.listBooks(carol).get(1).available());
    }

    @Test
    void returnFailsForBooksTheUserDoesNotHold() {
        library.borrow(bob, "The Great Gatsby");

        assertEquals(new ReturnOutcome.NotBorrowed("The Great Gatsby"), library.returnBook(carol, "The Great Gatsby"));
        assertEquals(new ReturnOutcome.NotBorrowed("1984"), library.returnBook(carol, "1984"));
        assertEquals(new ReturnOutcome.NotRegistered("Dune"), library.returnBook(carol, "Dune"));
    }

    @Test
    void statusListsBorrowedBooksAndWaitlists() {
        library.borrow(alice, "1984");
        library.borrow(bob, "The Great Gatsby");
        library.joinWaitlist(bob, "1984");

        StatusResponse status = library.status(bob);

        assertEquals(List.of("The Great Gatsby"), status.borrowed());
        assertEquals(List.of(new WaitlistPosition("1984", 1)), status.waitlists());
    }

    @Test
    void detailShowsWaitlistInfoAndHidesBorrowerFromMembers() {
        library.borrow(alice, "1984");
        library.joinWaitlist(bob, "1984");

        BookDetail forBob = library.bookDetail(bob, "1984");
        assertEquals(new BookDetail("1984", false, false, null, 1, 1), forBob);

        BookDetail forAlice = library.bookDetail(alice, "1984");
        assertEquals(new BookDetail("1984", false, true, null, 1, null), forAlice);
    }

    @Test
    void detailShowsBorrowerToAdmin() {
        library.borrow(alice, "1984");

        assertEquals("Alice", library.bookDetail(admin, "1984").borrowedBy());
        assertEquals(null, library.bookDetail(admin, "The Great Gatsby").borrowedBy());
    }

    @Test
    void detailOfUnknownBookIsNotFound() {
        assertThrows(NotFoundException.class, () -> library.bookDetail(alice, "Dune"));
    }
}
