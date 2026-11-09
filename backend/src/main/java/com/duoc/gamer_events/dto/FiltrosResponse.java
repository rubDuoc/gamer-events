package com.duoc.gamer_events.dto;

import java.util.List;

// Opciones para los filtros de busqueda de eventos
public record FiltrosResponse(List<String> categorias, List<String> ubicaciones) {
}
