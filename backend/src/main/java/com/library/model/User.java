package com.library.model;

import java.util.ArrayList;
import java.util.List;

public class User {

    public static final String ADMIN = "admin";

    private final String name;
    private final List<String> notifications = new ArrayList<>();

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String key() {
        return Keys.of(name);
    }

    public boolean isAdmin() {
        return ADMIN.equals(key());
    }

    public void notify(String message) {
        notifications.add(message);
    }

    public List<String> drainNotifications() {
        List<String> drained = List.copyOf(notifications);
        notifications.clear();
        return drained;
    }
}
