package com.library.dto;

public record BookDetail(
        String title,
        boolean available,
        boolean borrowedByMe,
        String borrowedBy,
        int waitlistSize,
        Integer waitlistPosition) {
}
