package com.duoc.gamer_events.dto;

import com.duoc.gamer_events.model.Evento;

import java.util.List;

// Datos publicos de la pagina de inicio
public record InicioResponse(List<Evento> proximos, List<Evento> destacados, List<String> marcas) {
}
