package com.duoc.gamer_events_web.client;

import com.duoc.gamer_events_web.model.Evento;
import com.duoc.gamer_events_web.model.EventoForm;
import com.duoc.gamer_events_web.model.Perfil;
import com.duoc.gamer_events_web.model.RegistroForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Cliente HTTP hacia la API REST del backend. Las rutas privadas envian el JWT en el header Authorization.
// Si la API responde con error (401, 404, 409...) se lanza una HttpClientErrorException.
@Component
public class BackendClient {

    private final RestClient restClient;

    public BackendClient(@Value("${backend.url}") String backendUrl) {
        this.restClient = RestClient.builder().baseUrl(backendUrl).build();
    }

    public Respuestas.Token login(String email, String password) {
        return restClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", email, "password", password))
                .retrieve().body(Respuestas.Token.class);
    }

    public void registrar(RegistroForm form) {
        restClient.post().uri("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve().toBodilessEntity();
    }

    public Respuestas.Inicio inicio() {
        return restClient.get().uri("/api/inicio").retrieve().body(Respuestas.Inicio.class);
    }

    public List<Evento> eventos(String token, String categoria, LocalDate fecha, String ubicacion) {
        return restClient.get()
                .uri(uri -> uri.path("/api/eventos")
                        .queryParamIfPresent("categoria", valor(categoria))
                        .queryParamIfPresent("fecha", Optional.ofNullable(fecha))
                        .queryParamIfPresent("ubicacion", valor(ubicacion))
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(new ParameterizedTypeReference<List<Evento>>() { });
    }

    public Respuestas.Filtros filtros(String token) {
        return restClient.get().uri("/api/eventos/filtros")
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(Respuestas.Filtros.class);
    }

    public Evento evento(String token, Long id) {
        return restClient.get().uri("/api/eventos/{id}", id)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(Evento.class);
    }

    public Perfil perfil(String token) {
        return restClient.get().uri("/api/perfil")
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(Perfil.class);
    }

    public Respuestas.OpcionesPerfil opcionesPerfil(String token) {
        return restClient.get().uri("/api/perfil/opciones")
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(Respuestas.OpcionesPerfil.class);
    }

    public void guardarPerfil(String token, Perfil perfil) {
        restClient.put().uri("/api/perfil")
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(perfil)
                .retrieve().toBodilessEntity();
    }

    // ---------------- Funcionalidades de comunidad (experiencia 2), todas con el JWT del usuario

    public Evento publicarEvento(String token, EventoForm form) {
        return restClient.post().uri("/api/eventos")
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(form.aPeticion())
                .retrieve().body(Evento.class);
    }

    public List<Respuestas.Media> media(String token, Long eventoId) {
        return restClient.get().uri("/api/eventos/{id}/media", eventoId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(new ParameterizedTypeReference<List<Respuestas.Media>>() { });
    }

    // La foto se reenvia con un nombre fijo: el nombre de archivo del usuario nunca se usa
    public void subirFoto(String token, Long eventoId, MultipartFile archivo) {
        MultiValueMap<String, Object> partes = new LinkedMultiValueMap<>();
        partes.add("archivo", new ByteArrayResource(leer(archivo)) {
            @Override
            public String getFilename() {
                return "imagen";
            }
        });
        restClient.post().uri("/api/eventos/{id}/fotos", eventoId)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(partes)
                .retrieve().toBodilessEntity();
    }

    public ResponseEntity<byte[]> foto(String token, Long mediaId) {
        return restClient.get().uri("/api/media/{id}/archivo", mediaId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().toEntity(byte[].class);
    }

    public void agregarVideo(String token, Long eventoId, String url) {
        restClient.post().uri("/api/eventos/{id}/videos", eventoId)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("url", url == null ? "" : url))
                .retrieve().toBodilessEntity();
    }

    public void eliminarMedia(String token, Long mediaId) {
        restClient.delete().uri("/api/media/{id}", mediaId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().toBodilessEntity();
    }

    public List<Respuestas.Comentario> comentarios(String token, Long eventoId) {
        return restClient.get().uri("/api/eventos/{id}/comentarios", eventoId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(new ParameterizedTypeReference<List<Respuestas.Comentario>>() { });
    }

    public void comentar(String token, Long eventoId, String texto) {
        restClient.post().uri("/api/eventos/{id}/comentarios", eventoId)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("texto", texto == null ? "" : texto))
                .retrieve().toBodilessEntity();
    }

    public void eliminarComentario(String token, Long comentarioId) {
        restClient.delete().uri("/api/comentarios/{id}", comentarioId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().toBodilessEntity();
    }

    public Respuestas.Valoracion valoracion(String token, Long eventoId) {
        return restClient.get().uri("/api/eventos/{id}/valoracion", eventoId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve().body(Respuestas.Valoracion.class);
    }

    public void valorar(String token, Long eventoId, Integer puntaje) {
        restClient.post().uri("/api/eventos/{id}/valoracion", eventoId)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("puntaje", puntaje == null ? 0 : puntaje))
                .retrieve().toBodilessEntity();
    }

    public void compartir(String token, Long eventoId, String red) {
        restClient.post().uri("/api/eventos/{id}/compartir", eventoId)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("red", red))
                .retrieve().toBodilessEntity();
    }

    // Publico (sin token): lo que la comunidad compartio en el sitio, para el inicio
    public List<Respuestas.Compartido> compartidosRecientes() {
        return restClient.get().uri("/api/compartidos/recientes")
                .retrieve().body(new ParameterizedTypeReference<List<Respuestas.Compartido>>() { });
    }

    private static byte[] leer(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen", e);
        }
    }

    private static Optional<String> valor(String texto) {
        return texto == null || texto.isBlank() ? Optional.empty() : Optional.of(texto.trim());
    }
}
