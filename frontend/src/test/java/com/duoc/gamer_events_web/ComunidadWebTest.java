package com.duoc.gamer_events_web;

import com.duoc.gamer_events_web.client.BackendClient;
import com.duoc.gamer_events_web.client.Respuestas;
import com.duoc.gamer_events_web.model.Evento;
import com.duoc.gamer_events_web.security.UsuarioSesion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Paginas nuevas de la experiencia 2 (el backend se simula con un mock)
@SpringBootTest
@AutoConfigureMockMvc
class ComunidadWebTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BackendClient backendClient;

    @BeforeEach
    void respuestasDelBackend() {
        Evento evento = new Evento();
        evento.setId(7L);
        evento.setNombre("Torneo de Smash");
        evento.setCategoria("Torneo");
        evento.setFecha(LocalDate.now().plusDays(5));
        evento.setHora(LocalTime.of(16, 0));
        evento.setUbicacion("Santiago");
        evento.setDireccion("Av. Matta 123");
        evento.setDescripcion("Torneo de prueba");
        evento.setOrganizadores(List.of());
        evento.setExpositores(List.of());
        evento.setAutorEmail("jugador1@gamerevents.cl");
        when(backendClient.evento(TOKEN, 7L)).thenReturn(evento);
        when(backendClient.media(TOKEN, 7L)).thenReturn(List.of(new Respuestas.Media(1L, "VIDEO", "dQw4w9WgXcQ", "jugador1@gamerevents.cl", LocalDateTime.now())));
        when(backendClient.comentarios(TOKEN, 7L)).thenReturn(List.of(
                new Respuestas.Comentario(1L, "Atacante", "otro@mail.cl", "<script>alert(1)</script>", LocalDateTime.now())));
        when(backendClient.valoracion(TOKEN, 7L)).thenReturn(new Respuestas.Valoracion(4.5, 2, null));
        when(backendClient.inicio()).thenReturn(new Respuestas.Inicio(List.of(), List.of(), List.of()));
        when(backendClient.compartidosRecientes()).thenReturn(List.of());
    }

    @Test
    void paginasNuevasSonPrivadas() throws Exception {
        mvc.perform(get("/eventos/nuevo")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/eventos/7/compartir/x")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void detalleMuestraVideoYEscapaLosComentarios() throws Exception {
        mvc.perform(get("/eventos/7").with(authentication(sesion())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("youtube-nocookie.com/embed/dQw4w9WgXcQ")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(header().string("Content-Security-Policy", containsString("frame-src https://www.youtube-nocookie.com")));
    }

    @Test
    void publicarEventoInvalidoNoLlegaAlBackend() throws Exception {
        mvc.perform(post("/eventos/nuevo").with(csrf()).with(authentication(sesion()))
                        .param("nombre", "x").param("categoria", "Hackeo").param("fecha", "2020-01-01"))
                .andExpect(status().isOk());
        verify(backendClient, never()).publicarEvento(any(), any());
    }

    @Test
    void formulariosNuevosExigenTokenCsrf() throws Exception {
        mvc.perform(post("/eventos/7/comentarios").with(authentication(sesion())).param("texto", "Hola"))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/eventos/7/fotos").file(new MockMultipartFile("archivo", "a.png", "image/png", new byte[]{1}))
                        .with(authentication(sesion())))
                .andExpect(status().isForbidden());
        verify(backendClient, never()).comentar(any(), any(), any());
    }

    @Test
    void compartirEnRedSocialRedirigeAUnHostFijo() throws Exception {
        mvc.perform(get("/eventos/7/compartir/whatsapp").with(authentication(sesion())))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("https://wa.me/")));
        verify(backendClient).compartir(TOKEN, 7L, "WHATSAPP");
        mvc.perform(get("/eventos/7/compartir/sitio-malicioso").with(authentication(sesion())))
                .andExpect(status().isNotFound());
    }

    @Test
    void comentarConCsrfLlamaAlBackend() throws Exception {
        mvc.perform(post("/eventos/7/comentarios").with(csrf()).with(authentication(sesion())).param("texto", "Nos vemos"))
                .andExpect(redirectedUrl("/eventos/7#comentarios"));
        verify(backendClient).comentar(eq(TOKEN), eq(7L), eq("Nos vemos"));
    }

    private static UsernamePasswordAuthenticationToken sesion() {
        return UsernamePasswordAuthenticationToken.authenticated(
                new UsuarioSesion("jugador1@gamerevents.cl", "Jugador Uno", TOKEN), null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
