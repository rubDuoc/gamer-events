package com.duoc.gamer_events_web.client;

import com.duoc.gamer_events_web.model.Evento;
import com.duoc.gamer_events_web.model.Perfil;
import com.duoc.gamer_events_web.model.RegistroForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

    private static Optional<String> valor(String texto) {
        return texto == null || texto.isBlank() ? Optional.empty() : Optional.of(texto.trim());
    }
}
