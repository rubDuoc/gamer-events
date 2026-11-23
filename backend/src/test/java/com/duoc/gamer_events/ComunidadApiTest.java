package com.duoc.gamer_events;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Funcionalidades de la experiencia 2: publicar eventos, fotos y videos, comentarios, valoraciones y compartir
@SpringBootTest
@AutoConfigureMockMvc
class ComunidadApiTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Autowired
    private MockMvc mvc;

    @Test
    void lasNuevasApisRequierenToken() throws Exception {
        mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON).content(eventoJson()))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/eventos/1/comentarios")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/eventos/1/valoracion").contentType(MediaType.APPLICATION_JSON).content("{\"puntaje\":5}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/compartidos/recientes")).andExpect(status().isOk());
    }

    @Test
    void publicarEventoValidadoYConElAutorDelToken() throws Exception {
        String token = login("jugador1@gamerevents.cl", "Jugador1#2026");
        mvc.perform(post("/api/eventos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(eventoJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autorEmail").value("jugador1@gamerevents.cl"))
                .andExpect(jsonPath("$.destacado").value(false));

        mvc.perform(post("/api/eventos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"x\",\"categoria\":\"Hackeo\",\"fecha\":\"2020-01-01\",\"hora\":\"10:00\","
                                + "\"ubicacion\":\"<script>\",\"direccion\":\"\",\"descripcion\":\"corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.categoria").exists())
                .andExpect(jsonPath("$.detalles.fecha").exists())
                .andExpect(jsonPath("$.detalles.ubicacion").exists());
    }

    @Test
    void fotosYVideosSoloDelAutorYConFormatoValido() throws Exception {
        String autor = login("jugador1@gamerevents.cl", "Jugador1#2026");
        String otro = login("jugador2@gamerevents.cl", "Jugador2#2026");
        Long eventoId = publicarEvento(autor);

        // Imagen PNG real: aceptada y se entrega con su tipo verificado
        String foto = mvc.perform(multipart("/api/eventos/" + eventoId + "/fotos")
                        .file(new MockMultipartFile("archivo", "foto.png", "image/png", PNG))
                        .header("Authorization", "Bearer " + autor))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer fotoId = JsonPath.read(foto, "$.id");
        mvc.perform(get("/api/media/" + fotoId + "/archivo").header("Authorization", "Bearer " + autor))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        // Un script disfrazado de PNG se rechaza por su contenido real
        mvc.perform(multipart("/api/eventos/" + eventoId + "/fotos")
                        .file(new MockMultipartFile("archivo", "foto.png", "image/png", "<script>alert(1)</script>".getBytes()))
                        .header("Authorization", "Bearer " + autor))
                .andExpect(status().isBadRequest());

        // Otro usuario no puede agregar fotos al evento ajeno
        mvc.perform(multipart("/api/eventos/" + eventoId + "/fotos")
                        .file(new MockMultipartFile("archivo", "foto.png", "image/png", PNG))
                        .header("Authorization", "Bearer " + otro))
                .andExpect(status().isForbidden());

        // Videos: solo YouTube; se guarda unicamente el id
        mvc.perform(post("/api/eventos/" + eventoId + "/videos").header("Authorization", "Bearer " + autor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.videoId").value("dQw4w9WgXcQ"));
        mvc.perform(post("/api/eventos/" + eventoId + "/videos").header("Authorization", "Bearer " + autor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://sitio-malicioso.com/video\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/eventos/" + eventoId + "/media").header("Authorization", "Bearer " + otro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void comentariosConPermisosDeAutor() throws Exception {
        String jugador1 = login("jugador1@gamerevents.cl", "Jugador1#2026");
        String jugador2 = login("jugador2@gamerevents.cl", "Jugador2#2026");
        String admin = login("admin@gamerevents.cl", "Admin#2026");

        String comentario = mvc.perform(post("/api/eventos/2/comentarios").header("Authorization", "Bearer " + jugador1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"<b>Nos vemos alla!</b>\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autorEmail").value("jugador1@gamerevents.cl"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(comentario, "$.id");

        mvc.perform(post("/api/eventos/2/comentarios").header("Authorization", "Bearer " + jugador1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/comentarios/" + id).header("Authorization", "Bearer " + jugador2))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/comentarios/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void valoracionDeUnoACincoUnaPorUsuario() throws Exception {
        String token = login("jugador2@gamerevents.cl", "Jugador2#2026");
        mvc.perform(post("/api/eventos/3/valoracion").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"puntaje\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miPuntaje").value(4));
        mvc.perform(post("/api/eventos/3/valoracion").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"puntaje\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miPuntaje").value(5))
                .andExpect(jsonPath("$.cantidad").value(1));
        mvc.perform(post("/api/eventos/3/valoracion").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"puntaje\":9}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void compartirEnElSitioApareceEnRecientes() throws Exception {
        String token = login("jugador1@gamerevents.cl", "Jugador1#2026");
        mvc.perform(post("/api/eventos/1/compartir").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"red\":\"SITIO\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/eventos/1/compartir").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"red\":\"TIKTOK\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/compartidos/recientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    private Long publicarEvento(String token) throws Exception {
        String respuesta = mvc.perform(post("/api/eventos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(eventoJson()))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private static String eventoJson() {
        return "{\"nombre\":\"Torneo de Smash Bros\",\"categoria\":\"Torneo\",\"fecha\":\"" + LocalDate.now().plusDays(30)
                + "\",\"hora\":\"16:00\",\"ubicacion\":\"Santiago\",\"direccion\":\"Av. Matta 123, Santiago\","
                + "\"descripcion\":\"Torneo 1v1 de Smash Bros Ultimate para todo nivel\",\"organizadores\":[\"Club Smash\"],"
                + "\"expositores\":[],\"estacionamiento\":true,\"locomocionColectiva\":true}";
    }

    private String login(String email, String password) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }
}
