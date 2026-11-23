package com.duoc.gamer_events.dto;

import com.duoc.gamer_events.model.Comentario;
import com.duoc.gamer_events.model.Compartido;
import com.duoc.gamer_events.model.Media;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// Peticiones y respuestas de las funcionalidades de comunidad: comentarios, valoraciones, fotos, videos y compartidos
public final class ComunidadDtos {

    private ComunidadDtos() {
    }

    public record ComentarioRequest(
            @NotBlank(message = "El comentario no puede estar vacio")
            @Size(min = 3, max = 500, message = "El comentario debe tener entre 3 y 500 caracteres")
            String texto) {
    }

    // Respuesta sin datos sensibles del autor (solo nombre y correo, nunca la contraseña)
    public record ComentarioResponse(Long id, String autor, String autorEmail, String texto, LocalDateTime fecha) {
        public static ComentarioResponse from(Comentario c) {
            return new ComentarioResponse(c.getId(), c.getUsuario().getNombre(), c.getUsuario().getEmail(),
                    c.getTexto(), c.getFecha());
        }
    }

    public record ValoracionRequest(
            @NotNull(message = "El puntaje es obligatorio")
            @Min(value = 1, message = "El puntaje minimo es 1")
            @Max(value = 5, message = "El puntaje maximo es 5")
            Integer puntaje) {
    }

    public record ValoracionResponse(double promedio, long cantidad, Integer miPuntaje) {
    }

    public record VideoRequest(
            @NotBlank(message = "El enlace del video es obligatorio")
            @Size(max = 200, message = "El enlace es demasiado largo")
            String url) {
    }

    public record MediaResponse(Long id, Media.Tipo tipo, String videoId, String autorEmail, LocalDateTime fecha) {
        public static MediaResponse from(Media m) {
            return new MediaResponse(m.getId(), m.getTipo(), m.getVideoId(), m.getAutorEmail(), m.getFecha());
        }
    }

    public record CompartirRequest(@NotNull(message = "Indica donde se comparte") Compartido.Red red) {
    }

    public record CompartidoResponse(Long eventoId, String evento, String usuario, Compartido.Red red, LocalDateTime fecha) {
        public static CompartidoResponse from(Compartido c) {
            return new CompartidoResponse(c.getEvento().getId(), c.getEvento().getNombre(), c.getUsuario().getNombre(),
                    c.getRed(), c.getFecha());
        }
    }
}
