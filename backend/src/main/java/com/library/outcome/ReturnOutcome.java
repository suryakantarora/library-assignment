package com.library.outcome;

public sealed interface ReturnOutcome {

    record Returned(String title) implements ReturnOutcome {
    }

    record NotRegistered(String title) implements ReturnOutcome {
    }

    record NotBorrowed(String title) implements ReturnOutcome {
    }
}
