package com.duoc.gamer_events.controller;

import com.duoc.gamer_events.service.EventoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Controller
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/eventos")
    public String listar(@RequestParam(required = false) String categoria,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                         @RequestParam(required = false) String ubicacion,
                         Model model) {
        model.addAttribute("eventos", eventoService.buscar(categoria, fecha, ubicacion));
        model.addAttribute("categorias", eventoService.categorias());
        model.addAttribute("ubicaciones", eventoService.ubicaciones());
        model.addAttribute("categoria", categoria);
        model.addAttribute("fecha", fecha);
        model.addAttribute("ubicacion", ubicacion);
        return "eventos";
    }

    @GetMapping("/eventos/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("evento", eventoService.obtenerPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado")));
        return "evento-detalle";
    }
}
