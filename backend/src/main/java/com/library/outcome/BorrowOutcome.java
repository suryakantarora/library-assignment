package com.library.outcome;

public sealed interface BorrowOutcome {

    record Borrowed(String title) implements BorrowOutcome {
    }

    record NotRegistered(String title) implements BorrowOutcome {
    }

    record Unavailable(String title) implements BorrowOutcome {
    }

    record AlreadyBorrowed(String title) implements BorrowOutcome {
    }
}
