package com.library.exception;

public abstract class LibraryException extends RuntimeException {

    protected LibraryException(String message) {
        super(message);
    }
}
