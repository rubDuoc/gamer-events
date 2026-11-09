package com.duoc.gamer_events;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSeguridadTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void inicioEsPublicoYSaleDeLaBaseDeDatos() throws Exception {
        mvc.perform(get("/api/inicio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destacados", hasSize(3)))
                .andExpect(jsonPath("$.marcas", hasSize(4)));
    }

    @Test
    void apisPrivadasSinTokenResponden401() throws Exception {
        mvc.perform(get("/api/eventos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/eventos/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/perfil")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/usuarios")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginEntregaTokenYPermiteUsarApisPrivadas() throws Exception {
        String token = login("jugador1@gamerevents.cl", "Jugador1#2026");

        mvc.perform(get("/api/eventos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
        mvc.perform(get("/api/eventos").param("categoria", "Torneo").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/api/eventos/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizadores", hasItem("Liga Gamer Chile")));
        mvc.perform(get("/api/eventos/999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void losTresUsuariosPuedenIniciarSesion() throws Exception {
        login("jugador1@gamerevents.cl", "Jugador1#2026");
        login("jugador2@gamerevents.cl", "Jugador2#2026");
        login("admin@gamerevents.cl", "Admin#2026");
    }

    @Test
    void loginConCredencialesInvalidasResponde401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jugador1@gamerevents.cl\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tokenAlteradoEsRechazado() throws Exception {
        String token = login("jugador1@gamerevents.cl", "Jugador1#2026");
        String alterado = token.substring(0, token.length() - 4) + "abcd";

        mvc.perform(get("/api/eventos").header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void soloElAdminPuedeVerLosUsuarios() throws Exception {
        String tokenUsuario = login("jugador1@gamerevents.cl", "Jugador1#2026");
        String tokenAdmin = login("admin@gamerevents.cl", "Admin#2026");

        mvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void perfilSeGuardaParaElUsuarioDelToken() throws Exception {
        String token = login("jugador2@gamerevents.cl", "Jugador2#2026");

        mvc.perform(put("/api/perfil").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"jugador_dos\",\"avatar\":\"🐉\",\"juegos\":[\"Valorant\",\"Juego falso\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("jugador_dos"))
                .andExpect(jsonPath("$.juegos", hasSize(1)));

        mvc.perform(get("/api/perfil").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.avatar").value("🐉"));
    }

    @Test
    void registroPorApiCreaUsuarioQuePuedeIniciarSesion() throws Exception {
        String body = "{\"nombre\":\"Api Gamer\",\"email\":\"api@gamerevents.cl\",\"password\":\"ClaveSegura123\"}";
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());

        login("api@gamerevents.cl", "ClaveSegura123");
    }

    private String login(String email, String password) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }
}
