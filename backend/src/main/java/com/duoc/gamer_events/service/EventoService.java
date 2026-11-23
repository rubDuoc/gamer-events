package com.duoc.gamer_events.service;

import com.duoc.gamer_events.dto.EventoRequest;
import com.duoc.gamer_events.model.Evento;
import com.duoc.gamer_events.model.Marca;
import com.duoc.gamer_events.repository.EventoRepository;
import com.duoc.gamer_events.repository.MarcaRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final MarcaRepository marcaRepository;

    public EventoService(EventoRepository eventoRepository, MarcaRepository marcaRepository) {
        this.eventoRepository = eventoRepository;
        this.marcaRepository = marcaRepository;
    }

    public List<Evento> buscar(String categoria, LocalDate fecha, String ubicacion) {
        return eventoRepository.buscar(vacioANulo(categoria), fecha, vacioANulo(ubicacion));
    }

    // [Privada] Publicar un evento: el autor es el usuario autenticado (nunca uno enviado en la peticion)
    public Evento publicar(EventoRequest r, String autorEmail) {
        Evento evento = new Evento(null, r.nombre().trim(), r.categoria(), r.fecha(), r.hora(), r.ubicacion().trim(),
                r.direccion().trim(), r.descripcion().trim(), limpiar(r.organizadores()), limpiar(r.expositores()),
                si(r.estacionamiento()), si(r.locomocionColectiva()), si(r.servicioComidas()), si(r.alojamiento()),
                false, autorEmail);
        return eventoRepository.save(evento);
    }

    private static boolean si(Boolean valor) {
        return Boolean.TRUE.equals(valor);
    }

    private static List<String> limpiar(List<String> valores) {
        return valores == null ? new ArrayList<>()
                : new ArrayList<>(valores.stream().map(String::trim).filter(v -> !v.isEmpty()).distinct().toList());
    }

    public Optional<Evento> obtenerPorId(Long id) {
        return eventoRepository.findById(id);
    }

    public List<Evento> proximos(int cantidad) {
        return eventoRepository.findByFechaGreaterThanEqualOrderByFechaAsc(LocalDate.now(), Limit.of(cantidad));
    }

    public List<Evento> destacados() {
        return eventoRepository.findByDestacadoTrueOrderByFechaAsc();
    }

    public List<String> categorias() {
        return eventoRepository.categorias();
    }

    public List<String> ubicaciones() {
        return eventoRepository.ubicaciones();
    }

    public List<String> marcas() {
        return marcaRepository.findAll().stream().map(Marca::getNombre).toList();
    }

    private String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
