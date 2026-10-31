package com.library.repository;

import com.library.model.Keys;
import com.library.model.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findByName(String name) {
        return Optional.ofNullable(users.get(Keys.of(name)));
    }

    @Override
    public User findOrCreate(String name) {
        return users.computeIfAbsent(Keys.of(name), key -> new User(name.trim()));
    }
}
