package com.library.outcome;

public sealed interface AddBookOutcome {

    record Added(String title) implements AddBookOutcome {
    }

    record AlreadyExists(String title) implements AddBookOutcome {
    }
}
