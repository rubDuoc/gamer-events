package com.duoc.gamer_events_web;

import com.duoc.gamer_events_web.client.BackendClient;
import com.duoc.gamer_events_web.client.Respuestas;
import com.duoc.gamer_events_web.model.Perfil;
import com.duoc.gamer_events_web.model.RegistroForm;
import com.duoc.gamer_events_web.security.UsuarioSesion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// El backend se simula con un mock: estas pruebas validan la seguridad del frontend
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadWebTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BackendClient backendClient;

    @BeforeEach
    void respuestasDelBackend() {
        when(backendClient.inicio()).thenReturn(new Respuestas.Inicio(List.of(), List.of(), List.of("Marca")));
        when(backendClient.filtros(TOKEN)).thenReturn(new Respuestas.Filtros(List.of("Torneo"), List.of("Santiago")));
        when(backendClient.eventos(eq(TOKEN), any(), any(), any())).thenReturn(List.of());
        when(backendClient.perfil(TOKEN)).thenReturn(new Perfil());
        when(backendClient.opcionesPerfil(TOKEN)).thenReturn(new Respuestas.OpcionesPerfil(List.of("Valorant"), List.of("🎮")));
    }

    @Test
    void paginasPublicasSinLogin() throws Exception {
        mvc.perform(get("/home")).andExpect(status().isOk());
        mvc.perform(get("/registro")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/css/style.css")).andExpect(status().isOk());
    }

    @Test
    void paginasPrivadasRedirigenAlLogin() throws Exception {
        mvc.perform(get("/eventos")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/eventos/1")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/perfil")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginValidaContraElBackend() throws Exception {
        when(backendClient.login("jugador1@gamerevents.cl", "Jugador1#2026")).thenReturn(
                new Respuestas.Token(TOKEN, "Bearer", 3600, "jugador1@gamerevents.cl", "Jugador Uno", List.of("ROLE_USER")));
        when(backendClient.login("jugador1@gamerevents.cl", "incorrecta"))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "", null, null, null));

        mvc.perform(formLogin().user("jugador1@gamerevents.cl").password("Jugador1#2026"))
                .andExpect(authenticated().withUsername("jugador1@gamerevents.cl"));
        mvc.perform(formLogin().user("jugador1@gamerevents.cl").password("incorrecta"))
                .andExpect(unauthenticated());
    }

    @Test
    void usuarioAutenticadoUsaSuTokenParaLlamarALaApi() throws Exception {
        mvc.perform(get("/eventos").param("categoria", "Torneo").with(authentication(sesion())))
                .andExpect(status().isOk());
        mvc.perform(get("/perfil").with(authentication(sesion()))).andExpect(status().isOk());

        verify(backendClient).eventos(eq(TOKEN), eq("Torneo"), isNull(), isNull());
        verify(backendClient).perfil(TOKEN);
    }

    @Test
    void tokenExpiradoCierraLaSesion() throws Exception {
        when(backendClient.filtros(TOKEN))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "", null, null, null));

        mvc.perform(get("/eventos").with(authentication(sesion())))
                .andExpect(redirectedUrl("/login?expirada"));
    }

    @Test
    void registroEnviaLosDatosAlBackend() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Nuevo Gamer")
                        .param("email", "nuevo@gamerevents.cl")
                        .param("password", "ClaveSegura123"))
                .andExpect(redirectedUrl("/login?registrado"));

        verify(backendClient).registrar(any(RegistroForm.class));
    }

    @Test
    void registroInvalidoODuplicadoNoRedirige() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "")
                        .param("email", "no-es-correo")
                        .param("password", "123"))
                .andExpect(status().isOk());
        verify(backendClient, never()).registrar(any());

        doThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "", null, null, null))
                .when(backendClient).registrar(any());
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Repetido")
                        .param("email", "jugador1@gamerevents.cl")
                        .param("password", "ClaveSegura123"))
                .andExpect(status().isOk());
    }

    @Test
    void formularioSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/registro")
                        .param("nombre", "Atacante")
                        .param("email", "atacante@mail.com")
                        .param("password", "ClaveSegura123"))
                .andExpect(status().isForbidden());
        verify(backendClient, never()).registrar(any());
    }

    private static UsernamePasswordAuthenticationToken sesion() {
        return UsernamePasswordAuthenticationToken.authenticated(
                new UsuarioSesion("jugador1@gamerevents.cl", "Jugador Uno", TOKEN), null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
