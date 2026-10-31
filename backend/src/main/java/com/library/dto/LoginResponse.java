package com.library.dto;

import java.util.List;

public record LoginResponse(String token, String name, boolean admin, List<String> notifications, StatusResponse status) {
}
