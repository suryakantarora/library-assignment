package com.library.outcome;

public sealed interface WaitlistOutcome {

    record Added(String title, int position) implements WaitlistOutcome {
    }

    record NotRegistered(String title) implements WaitlistOutcome {
    }

    record BookAvailable(String title) implements WaitlistOutcome {
    }

    record AlreadyBorrowed(String title) implements WaitlistOutcome {
    }

    record AlreadyWaiting(String title, int position) implements WaitlistOutcome {
    }
}
