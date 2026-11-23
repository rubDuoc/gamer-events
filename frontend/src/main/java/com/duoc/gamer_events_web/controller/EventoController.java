package com.duoc.gamer_events_web.controller;

import com.duoc.gamer_events_web.client.BackendClient;
import com.duoc.gamer_events_web.client.Respuestas;
import com.duoc.gamer_events_web.model.Evento;
import com.duoc.gamer_events_web.model.EventoForm;
import com.duoc.gamer_events_web.security.UsuarioSesion;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@Controller
public class EventoController {

    // Redes sociales permitidas: el destino se arma en el servidor con un host fijo (no hay redireccion abierta)
    private static final Map<String, String> REDES = Map.of(
            "X", "https://twitter.com/intent/tweet?text={texto}&url={url}",
            "FACEBOOK", "https://www.facebook.com/sharer/sharer.php?u={url}",
            "WHATSAPP", "https://wa.me/?text={texto}%20{url}");

    private final BackendClient backendClient;

    public EventoController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    @GetMapping("/eventos")
    public String listar(@RequestParam(required = false) String categoria,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         @RequestParam(required = false) String ubicacion,
                         @AuthenticationPrincipal UsuarioSesion usuario,
                         Model model) {
        Respuestas.Filtros filtros = backendClient.filtros(usuario.token());
        model.addAttribute("eventos", backendClient.eventos(usuario.token(), categoria, fecha, ubicacion));
        model.addAttribute("categorias", filtros.categorias());
        model.addAttribute("ubicaciones", filtros.ubicaciones());
        model.addAttribute("categoria", categoria);
        model.addAttribute("fecha", fecha);
        model.addAttribute("ubicacion", ubicacion);
        return "eventos";
    }

    // ---------------- [Privada] Publicar evento
    @GetMapping("/eventos/nuevo")
    public String formularioEvento(Model model) {
        model.addAttribute("eventoForm", new EventoForm());
        model.addAttribute("categorias", EventoForm.CATEGORIAS);
        return "evento-nuevo";
    }

    @PostMapping("/eventos/nuevo")
    public String publicar(@Valid @ModelAttribute EventoForm eventoForm, BindingResult result,
                           @AuthenticationPrincipal UsuarioSesion usuario, Model model) {
        if (!result.hasErrors()) {
            try {
                Evento evento = backendClient.publicarEvento(usuario.token(), eventoForm);
                return "redirect:/eventos/" + evento.getId() + "?publicado";
            } catch (HttpClientErrorException.BadRequest e) {
                result.reject("api", mensaje(e));
            }
        }
        model.addAttribute("categorias", EventoForm.CATEGORIAS);
        return "evento-nuevo";
    }

    // ---------------- [Privada] Detalle con fotos, videos, valoracion, comentarios y compartir
    @GetMapping("/eventos/{id}")
    public String detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioSesion usuario,
                          Authentication auth, Model model) {
        Evento evento;
        try {
            evento = backendClient.evento(usuario.token(), id);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }
        model.addAttribute("evento", evento);
        model.addAttribute("media", backendClient.media(usuario.token(), id));
        model.addAttribute("comentarios", backendClient.comentarios(usuario.token(), id));
        model.addAttribute("valoracion", backendClient.valoracion(usuario.token(), id));
        model.addAttribute("esAdmin", esAdmin(auth));
        model.addAttribute("puedeEditarMedia", esAdmin(auth) || usuario.email().equals(evento.getAutorEmail()));
        model.addAttribute("emailUsuario", usuario.email());
        return "evento-detalle";
    }

    @PostMapping("/eventos/{id}/comentarios")
    public String comentar(@PathVariable Long id, @RequestParam String texto,
                           @AuthenticationPrincipal UsuarioSesion usuario, RedirectAttributes flash) {
        return ejecutar(id, flash, "Comentario publicado", "#comentarios", () -> backendClient.comentar(usuario.token(), id, texto));
    }

    @PostMapping("/eventos/{id}/comentarios/{comentarioId}/eliminar")
    public String eliminarComentario(@PathVariable Long id, @PathVariable Long comentarioId,
                                     @AuthenticationPrincipal UsuarioSesion usuario, RedirectAttributes flash) {
        return ejecutar(id, flash, "Comentario eliminado", "#comentarios",
                () -> backendClient.eliminarComentario(usuario.token(), comentarioId));
    }

    @PostMapping("/eventos/{id}/valoracion")
    public String valorar(@PathVariable Long id, @RequestParam(required = false) Integer puntaje,
                          @AuthenticationPrincipal UsuarioSesion usuario, RedirectAttributes flash) {
        return ejecutar(id, flash, "Gracias por tu valoracion", "#valoracion", () -> backendClient.valorar(usuario.token(), id, puntaje));
    }

    @PostMapping("/eventos/{id}/fotos")
    public String subirFoto(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo,
                            @AuthenticationPrincipal UsuarioSesion usuario, RedirectAttributes flash) {
        if (archivo.isEmpty()) {
            flash.addFlashAttribute("error", "Selecciona una imagen");
            return "redirect:/eventos/" + id + "#galeria";
        }
        return ejecutar(id, flash, "Foto agregada", "#galeria", () -> backendClient.subirFoto(usuario.token(), id, archivo));
    }

    @PostMapping("/eventos/{id}/videos")
    public String agregarVideo(@PathVariable Long id, @RequestParam String url,
                               @AuthenticationPrincipal UsuarioSesion usuario, RedirectAttributes flash) {
        return ejecutar(id, flash, "Video agregado", "#galeria", () -> backendClient.agregarVideo(usuario.token(), id, url));
    }

    @PostMapping("/eventos/{id}/media/{mediaId}/eliminar")
    public String eliminarMedia(@PathVariable Long id, @PathVariable Long mediaId,
                                @AuthenticationPrincipal UsuarioSesion usuario, RedirectAttributes flash) {
        return ejecutar(id, flash, "Contenido eliminado", "#galeria", () -> backendClient.eliminarMedia(usuario.token(), mediaId));
    }

    // Las fotos se piden al backend con el token del usuario; el navegador nunca ve el JWT
    @GetMapping("/eventos/{id}/fotos/{mediaId}")
    public ResponseEntity<byte[]> foto(@PathVariable Long id, @PathVariable Long mediaId,
                                       @AuthenticationPrincipal UsuarioSesion usuario) {
        ResponseEntity<byte[]> foto = backendClient.foto(usuario.token(), mediaId);
        return ResponseEntity.ok()
                .contentType(foto.getHeaders().getContentType())
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(foto.getBody());
    }

    // Compartir en el sitio: aparece en "Compartidos por la comunidad" del inicio
    @PostMapping("/eventos/{id}/compartir")
    public String compartirEnSitio(@PathVariable Long id, @AuthenticationPrincipal UsuarioSesion usuario, RedirectAttributes flash) {
        return ejecutar(id, flash, "Compartiste el evento con la comunidad de Gamer Events", "#compartir",
                () -> backendClient.compartir(usuario.token(), id, "SITIO"));
    }

    // Compartir en una red social: se registra y se redirige al sitio de la red con el enlace del evento
    @GetMapping("/eventos/{id}/compartir/{red}")
    public String compartirEnRed(@PathVariable Long id, @PathVariable String red,
                                 @AuthenticationPrincipal UsuarioSesion usuario) {
        String plantilla = REDES.get(red.toUpperCase());
        if (plantilla == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Red social no soportada");
        }
        Evento evento = backendClient.evento(usuario.token(), id);
        backendClient.compartir(usuario.token(), id, red.toUpperCase());
        String enlace = ServletUriComponentsBuilder.fromCurrentContextPath().path("/eventos/{id}").buildAndExpand(id).toUriString();
        String destino = UriComponentsBuilder.fromUriString(plantilla)
                .buildAndExpand(Map.of("texto", "Mira este evento en Gamer Events: " + evento.getNombre(), "url", enlace))
                .encode().toUriString();
        return "redirect:" + destino;
    }

    // Ejecuta la accion en el backend y muestra el resultado (o el mensaje de error de la API) en la pagina del evento
    private String ejecutar(Long id, RedirectAttributes flash, String exito, String ancla, Runnable accion) {
        try {
            accion.run();
            flash.addFlashAttribute("mensaje", exito);
        } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.Forbidden | HttpClientErrorException.NotFound e) {
            flash.addFlashAttribute("error", mensaje(e));
        }
        return "redirect:/eventos/" + id + ancla;
    }

    private static String mensaje(HttpClientErrorException e) {
        try {
            Respuestas.ErrorApi error = e.getResponseBodyAs(Respuestas.ErrorApi.class);
            return error != null && error.mensaje() != null ? error.mensaje() : "No se pudo completar la accion";
        } catch (RuntimeException ex) {
            return "No se pudo completar la accion";
        }
    }

    private static boolean esAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
