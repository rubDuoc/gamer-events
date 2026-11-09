package com.duoc.gamer_events.dto;

import java.util.List;

// Juegos y avatares permitidos en el perfil
public record OpcionesPerfilResponse(List<String> juegos, List<String> avatares) {
}
