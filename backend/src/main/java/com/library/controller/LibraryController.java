package com.library.controller;

import com.library.config.AuthInterceptor;
import com.library.dto.ActionResponse;
import com.library.dto.BookDetail;
import com.library.dto.BookView;
import com.library.dto.StatusResponse;
import com.library.dto.TitleRequest;
import com.library.model.User;
import com.library.outcome.AddBookOutcome;
import com.library.outcome.BorrowOutcome;
import com.library.outcome.ReturnOutcome;
import com.library.outcome.WaitlistOutcome;
import com.library.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LibraryController {

    private final LibraryService library;

    public LibraryController(LibraryService library) {
        this.library = library;
    }

    @GetMapping("/books")
    public List<BookView> list(@RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user) {
        return library.listBooks(user);
    }

    @GetMapping("/books/detail")
    public BookDetail detail(
            @RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
            @RequestParam String title) {
        return library.bookDetail(user, title);
    }

    @PostMapping("/books")
    public ResponseEntity<ActionResponse> add(
            @RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
            @Valid @RequestBody TitleRequest request) {
        return switch (library.addBook(user, request.title())) {
            case AddBookOutcome.Added(String title) ->
                    reply(HttpStatus.CREATED, "Book \"%s\" has been added to the library.", title);
            case AddBookOutcome.AlreadyExists(String title) ->
                    reply(HttpStatus.CONFLICT, "Book \"%s\" already exists in the library.", title);
        };
    }

    @PostMapping("/borrow")
    public ResponseEntity<ActionResponse> borrow(
            @RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
            @Valid @RequestBody TitleRequest request) {
        return switch (library.borrow(user, request.title())) {
            case BorrowOutcome.Borrowed(String title) ->
                    reply(HttpStatus.OK, "You borrowed \"%s\".", title);
            case BorrowOutcome.NotRegistered(String title) ->
                    reply(HttpStatus.NOT_FOUND, "Sorry, \"%s\" is not registered.", title);
            case BorrowOutcome.Unavailable(String title) ->
                    reply(HttpStatus.CONFLICT, "Sorry, \"%s\" is currently not available.", title);
            case BorrowOutcome.AlreadyBorrowed(String title) ->
                    reply(HttpStatus.CONFLICT, "You have already borrowed \"%s\".", title);
        };
    }

    @PostMapping("/return")
    public ResponseEntity<ActionResponse> returnBook(
            @RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
            @Valid @RequestBody TitleRequest request) {
        return switch (library.returnBook(user, request.title())) {
            case ReturnOutcome.Returned(String title) ->
                    reply(HttpStatus.OK, "You returned \"%s\".", title);
            case ReturnOutcome.NotRegistered(String title) ->
                    reply(HttpStatus.NOT_FOUND, "Sorry, \"%s\" is not registered.", title);
            case ReturnOutcome.NotBorrowed(String title) ->
                    reply(HttpStatus.CONFLICT, "Sorry, you didn't borrow \"%s\"", title);
        };
    }

    @PostMapping("/waitlist")
    public ResponseEntity<ActionResponse> waitlist(
            @RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
            @Valid @RequestBody TitleRequest request) {
        return switch (library.joinWaitlist(user, request.title())) {
            case WaitlistOutcome.Added(String title, int position) ->
                    reply(HttpStatus.OK, "You are added to the wait list of \"%s\", your position is %d.", title, position);
            case WaitlistOutcome.NotRegistered(String title) ->
                    reply(HttpStatus.NOT_FOUND, "Sorry, \"%s\" is not registered.", title);
            case WaitlistOutcome.BookAvailable(String title) ->
                    reply(HttpStatus.CONFLICT, "\"%s\" is available right now, you can borrow it instead.", title);
            case WaitlistOutcome.AlreadyBorrowed(String title) ->
                    reply(HttpStatus.CONFLICT, "You have already borrowed \"%s\".", title);
            case WaitlistOutcome.AlreadyWaiting(String title, int position) ->
                    reply(HttpStatus.CONFLICT, "You are already on the wait list of \"%s\", your position is %d.", title, position);
        };
    }

    @GetMapping("/status")
    public StatusResponse status(@RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user) {
        return library.status(user);
    }

    private ResponseEntity<ActionResponse> reply(HttpStatus status, String template, Object... args) {
        return ResponseEntity.status(status).body(new ActionResponse(template.formatted(args)));
    }
}
