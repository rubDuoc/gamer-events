package com.duoc.gamer_events.dto;

import java.util.Map;

public record ErrorResponse(int status, String error, Map<String, String> detalles) {

    public ErrorResponse(int status, String error) {
        this(status, error, Map.of());
    }
}
