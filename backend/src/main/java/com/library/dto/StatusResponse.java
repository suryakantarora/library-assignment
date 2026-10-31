package com.library.dto;

import java.util.List;

public record StatusResponse(List<String> borrowed, List<WaitlistPosition> waitlists) {
}
