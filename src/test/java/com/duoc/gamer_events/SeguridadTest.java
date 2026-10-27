package com.duoc.gamer_events;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadTest {

    @Autowired
    private MockMvc mvc;

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
    @WithMockUser("jugador1@gamerevents.cl")
    void usuarioAutenticadoAccedeAPaginasPrivadas() throws Exception {
        mvc.perform(get("/eventos")).andExpect(status().isOk());
        mvc.perform(get("/eventos").param("categoria", "Torneo").param("ubicacion", "Santiago"))
                .andExpect(status().isOk());
        mvc.perform(get("/eventos/1")).andExpect(status().isOk());
        mvc.perform(get("/perfil")).andExpect(status().isOk());
    }

    @Test
    void loginConUsuariosEnMemoria() throws Exception {
        mvc.perform(formLogin().user("jugador1@gamerevents.cl").password("Jugador1#2026")).andExpect(authenticated());
        mvc.perform(formLogin().user("jugador2@gamerevents.cl").password("Jugador2#2026")).andExpect(authenticated());
        mvc.perform(formLogin().user("admin@gamerevents.cl").password("Admin#2026")).andExpect(authenticated());
        mvc.perform(formLogin().user("admin@gamerevents.cl").password("incorrecta")).andExpect(unauthenticated());
    }

    @Test
    void registroCreaUsuarioQuePuedeIniciarSesion() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Nuevo Gamer")
                        .param("email", "nuevo@gamerevents.cl")
                        .param("password", "ClaveSegura123"))
                .andExpect(redirectedUrl("/login?registrado"));

        mvc.perform(formLogin().user("nuevo@gamerevents.cl").password("ClaveSegura123")).andExpect(authenticated());
    }

    @Test
    void registroInvalidoNoCreaUsuario() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "")
                        .param("email", "no-es-correo")
                        .param("password", "123"))
                .andExpect(status().isOk());

        mvc.perform(formLogin().user("no-es-correo").password("123")).andExpect(unauthenticated());
    }

    @Test
    void formularioSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/registro")
                        .param("nombre", "Atacante")
                        .param("email", "atacante@mail.com")
                        .param("password", "ClaveSegura123"))
                .andExpect(status().isForbidden());
    }
}
