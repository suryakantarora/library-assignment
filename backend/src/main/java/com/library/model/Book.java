package com.library.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Book {

    private final String title;
    private String borrowerKey;
    private final List<String> waitlist = new ArrayList<>();

    public Book(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String key() {
        return Keys.of(title);
    }

    public boolean isAvailable() {
        return borrowerKey == null;
    }

    public Optional<String> borrowerKey() {
        return Optional.ofNullable(borrowerKey);
    }

    public int waitlistSize() {
        return waitlist.size();
    }

    public boolean isBorrowedBy(User user) {
        return user.key().equals(borrowerKey);
    }

    public void lendTo(User user) {
        borrowerKey = user.key();
    }

    public void release() {
        borrowerKey = null;
    }

    public void enqueue(User user) {
        waitlist.add(user.key());
    }

    public Optional<Integer> positionOf(User user) {
        int index = waitlist.indexOf(user.key());
        return index < 0 ? Optional.empty() : Optional.of(index + 1);
    }

    public Optional<String> pollNextInLine() {
        return waitlist.isEmpty() ? Optional.empty() : Optional.of(waitlist.remove(0));
    }
}
