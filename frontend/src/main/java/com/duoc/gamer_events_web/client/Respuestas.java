package com.duoc.gamer_events_web.client;

import com.duoc.gamer_events_web.model.Evento;

import java.util.List;

// Respuestas JSON de la API del backend
public final class Respuestas {

    private Respuestas() {
    }

    public record Token(String token, String tipo, long expiraEnSegundos, String email, String nombre,
                        List<String> roles) {
    }

    public record Inicio(List<Evento> proximos, List<Evento> destacados, List<String> marcas) {
    }

    public record Filtros(List<String> categorias, List<String> ubicaciones) {
    }

    public record OpcionesPerfil(List<String> juegos, List<String> avatares) {
    }
}
