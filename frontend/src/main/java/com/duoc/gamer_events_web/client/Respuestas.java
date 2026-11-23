package com.duoc.gamer_events_web.client;

import com.duoc.gamer_events_web.model.Evento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    public record Comentario(Long id, String autor, String autorEmail, String texto, LocalDateTime fecha) {
    }

    public record Valoracion(double promedio, long cantidad, Integer miPuntaje) {
    }

    public record Media(Long id, String tipo, String videoId, String autorEmail, LocalDateTime fecha) {
    }

    public record Compartido(Long eventoId, String evento, String usuario, String red, LocalDateTime fecha) {
    }

    // Formato de error de la API: mensaje general y, si hubo validacion, el detalle por campo
    public record ErrorApi(String error, Map<String, String> detalles) {

        public String mensaje() {
            return detalles != null && !detalles.isEmpty() ? detalles.values().iterator().next() : error;
        }
    }
}
