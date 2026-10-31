package com.library.controller;

import com.library.config.AuthInterceptor;
import com.library.dto.ActionResponse;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.model.User;
import com.library.service.LibraryService;
import com.library.service.SessionRegistry;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LibraryService library;
    private final SessionRegistry sessions;

    public AuthController(LibraryService library, SessionRegistry sessions) {
        this.library = library;
        this.sessions = sessions;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User user = library.login(request.name());
        String token = sessions.open(user);
        return new LoginResponse(
                token,
                user.getName(),
                user.isAdmin(),
                library.collectNotifications(user),
                library.status(user));
    }

    @PostMapping("/logout")
    public ActionResponse logout(
            @RequestAttribute(AuthInterceptor.USER_ATTRIBUTE) User user,
            @RequestHeader(AuthInterceptor.TOKEN_HEADER) String token) {
        sessions.close(token);
        return new ActionResponse("Goodbye, %s!".formatted(user.getName()));
    }
}
