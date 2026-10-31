package com.library.dto;

public record BookView(String title, boolean available, boolean borrowedByMe, Integer waitlistPosition) {
}
