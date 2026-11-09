package com.duoc.gamer_events_web.controller;

import com.duoc.gamer_events_web.client.BackendClient;
import com.duoc.gamer_events_web.client.Respuestas;
import com.duoc.gamer_events_web.security.UsuarioSesion;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Controller
public class EventoController {

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

    @GetMapping("/eventos/{id}")
    public String detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioSesion usuario, Model model) {
        try {
            model.addAttribute("evento", backendClient.evento(usuario.token(), id));
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }
        return "evento-detalle";
    }
}
