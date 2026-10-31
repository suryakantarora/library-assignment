package com.library.service;

import com.library.model.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    public String open(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public Optional<User> find(String token) {
        return token == null ? Optional.empty() : Optional.ofNullable(sessions.get(token));
    }

    public void close(String token) {
        sessions.remove(token);
    }
}
