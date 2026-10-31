package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TitleRequest(@NotBlank @Size(max = 150) String title) {
}
