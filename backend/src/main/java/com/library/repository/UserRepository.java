package com.library.repository;

import com.library.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByName(String name);

    User findOrCreate(String name);
}
