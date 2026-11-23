package com.duoc.gamer_events.controller.api;

import com.duoc.gamer_events.dto.ComunidadDtos.CompartidoResponse;
import com.duoc.gamer_events.dto.ComunidadDtos.CompartirRequest;
import com.duoc.gamer_events.dto.ComunidadDtos.ComentarioRequest;
import com.duoc.gamer_events.dto.ComunidadDtos.ComentarioResponse;
import com.duoc.gamer_events.dto.ComunidadDtos.MediaResponse;
import com.duoc.gamer_events.dto.ComunidadDtos.ValoracionRequest;
import com.duoc.gamer_events.dto.ComunidadDtos.ValoracionResponse;
import com.duoc.gamer_events.dto.ComunidadDtos.VideoRequest;
import com.duoc.gamer_events.service.ComunidadService;
import com.duoc.gamer_events.service.MediaService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

// [Privadas, con token JWT] fotos y videos, comentarios, valoraciones y compartir eventos.
// [Publica] GET /api/compartidos/recientes (lo que la comunidad compartio en el sitio, para el inicio).
@RestController
@RequestMapping("/api")
public class ComunidadApiController {

    private final ComunidadService comunidadService;
    private final MediaService mediaService;

    public ComunidadApiController(ComunidadService comunidadService, MediaService mediaService) {
        this.comunidadService = comunidadService;
        this.mediaService = mediaService;
    }

    // ---------------- Fotos y videos
    @GetMapping("/eventos/{id}/media")
    public List<MediaResponse> media(@PathVariable Long id) {
        return mediaService.listar(id);
    }

    @PostMapping(value = "/eventos/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponse subirFoto(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo, Authentication auth) {
        return mediaService.subirFoto(id, archivo, auth.getName(), esAdmin(auth));
    }

    @PostMapping("/eventos/{id}/videos")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponse agregarVideo(@PathVariable Long id, @Valid @RequestBody VideoRequest request, Authentication auth) {
        return mediaService.agregarVideo(id, request.url(), auth.getName(), esAdmin(auth));
    }

    // Se entrega con el tipo verificado al subir; nosniff (Spring Security) impide que el navegador lo reinterprete
    @GetMapping("/media/{id}/archivo")
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        MediaService.Archivo foto = mediaService.foto(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(foto.contenido());
    }

    @DeleteMapping("/media/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMedia(@PathVariable Long id, Authentication auth) {
        mediaService.eliminar(id, auth.getName(), esAdmin(auth));
    }

    // ---------------- Comentarios
    @GetMapping("/eventos/{id}/comentarios")
    public List<ComentarioResponse> comentarios(@PathVariable Long id) {
        return comunidadService.comentarios(id);
    }

    @PostMapping("/eventos/{id}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResponse comentar(@PathVariable Long id, @Valid @RequestBody ComentarioRequest request, Authentication auth) {
        return comunidadService.comentar(id, request.texto(), auth.getName());
    }

    @DeleteMapping("/comentarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarComentario(@PathVariable Long id, Authentication auth) {
        comunidadService.eliminarComentario(id, auth.getName(), esAdmin(auth));
    }

    // ---------------- Valoraciones
    @GetMapping("/eventos/{id}/valoracion")
    public ValoracionResponse valoracion(@PathVariable Long id, Authentication auth) {
        return comunidadService.resumenValoracion(id, auth.getName());
    }

    @PostMapping("/eventos/{id}/valoracion")
    public ValoracionResponse valorar(@PathVariable Long id, @Valid @RequestBody ValoracionRequest request, Authentication auth) {
        return comunidadService.valorar(id, request.puntaje(), auth.getName());
    }

    // ---------------- Compartir
    @PostMapping("/eventos/{id}/compartir")
    @ResponseStatus(HttpStatus.CREATED)
    public CompartidoResponse compartir(@PathVariable Long id, @Valid @RequestBody CompartirRequest request, Authentication auth) {
        return comunidadService.compartir(id, request.red(), auth.getName());
    }

    @GetMapping("/compartidos/recientes")
    public List<CompartidoResponse> compartidosRecientes() {
        return comunidadService.compartidosEnElSitio(6);
    }

    private static boolean esAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
