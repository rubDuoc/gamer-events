package com.duoc.gamer_events.dto;

import java.util.List;

public record TokenResponse(String token, String tipo, long expiraEnSegundos, String email, List<String> roles) {
}
