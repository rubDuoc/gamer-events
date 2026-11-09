package com.duoc.gamer_events.controller.api;

import com.duoc.gamer_events.dto.InicioResponse;
import com.duoc.gamer_events.model.Evento;
import com.duoc.gamer_events.service.EventoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EventoApiController {

    private final EventoService eventoService;

    public EventoApiController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    // [Publica] proximos eventos, destacados y marcas asociadas
    @GetMapping("/inicio")
    public InicioResponse inicio() {
        return new InicioResponse(eventoService.proximos(3), eventoService.destacados(), eventoService.marcas());
    }

    // [Privada] buscar eventos por categoria, fecha y ubicacion
    @GetMapping("/eventos")
    public List<Evento> buscar(@RequestParam(required = false) String categoria,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                               @RequestParam(required = false) String ubicacion) {
        return eventoService.buscar(categoria, fecha, ubicacion);
    }

    // [Privada] detalle de un evento
    @GetMapping("/eventos/{id}")
    public Evento detalle(@PathVariable Long id) {
        return eventoService.obtenerPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }
}
