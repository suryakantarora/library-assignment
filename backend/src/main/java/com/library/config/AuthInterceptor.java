package com.library.config;

import com.library.exception.UnauthorizedException;
import com.library.service.SessionRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String TOKEN_HEADER = "X-Auth-Token";
    public static final String USER_ATTRIBUTE = "currentUser";

    private final SessionRegistry sessions;

    public AuthInterceptor(SessionRegistry sessions) {
        this.sessions = sessions;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        var user = sessions.find(request.getHeader(TOKEN_HEADER))
                .orElseThrow(() -> new UnauthorizedException("Please login first."));
        request.setAttribute(USER_ATTRIBUTE, user);
        return true;
    }
}
