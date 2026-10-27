package com.duoc.gamer_events.service;

import com.duoc.gamer_events.model.Evento;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class EventoService {

    // Datos de ejemplo en memoria (aun no hay base de datos)
    private final List<Evento> eventos = List.of(
            new Evento(1L, "Torneo Regional de League of Legends", "Torneo",
                    LocalDate.of(2026, 11, 14), LocalTime.of(10, 0), "Santiago",
                    "Av. Libertador Bernardo O'Higgins 3363, Estacion Central",
                    "Torneo 5v5 abierto a equipos amateur. Fase de grupos en la manana y playoffs en la tarde.",
                    List.of("Liga Gamer Chile", "Duoc UC Esports"),
                    List.of("Caster Nicolas 'Nikko' Rojas", "Coach Valentina Paredes"),
                    true, true, true, false, true),
            new Evento(2L, "Expo Indie Games Valparaiso", "Exposicion",
                    LocalDate.of(2026, 11, 21), LocalTime.of(11, 0), "Valparaiso",
                    "Av. Brasil 2950, Valparaiso",
                    "Muestra de videojuegos independientes chilenos con zona de prueba y charlas de desarrolladores.",
                    List.of("Asociacion de Desarrolladores Indie"),
                    List.of("Estudio Pixel Austral", "Estudio Cordillera Games"),
                    false, true, true, false, true),
            new Evento(3L, "LAN Party Retro", "LAN Party",
                    LocalDate.of(2026, 11, 28), LocalTime.of(18, 0), "Concepcion",
                    "Barros Arana 1068, Concepcion",
                    "Noche de juegos clasicos en red: Counter-Strike 1.6, Age of Empires II y Quake III.",
                    List.of("Club Retro Biobio"),
                    List.of("Coleccionista Andres Fuentes"),
                    true, true, true, false, false),
            new Evento(4L, "Copa Speedrun Chile", "Torneo",
                    LocalDate.of(2026, 12, 5), LocalTime.of(15, 0), "Santiago",
                    "Av. Providencia 1308, Providencia",
                    "Competencia de speedrun en vivo con categorias Any% y 100% en juegos de plataformas.",
                    List.of("Speedrun Chile"),
                    List.of("Runner Camila 'Kami' Soto", "Runner Diego Vera"),
                    false, true, false, false, false),
            new Evento(5L, "Feria Cosplay & Gaming", "Convencion",
                    LocalDate.of(2026, 12, 12), LocalTime.of(12, 0), "Santiago",
                    "Av. Kennedy 9001, Las Condes",
                    "Convencion con concurso de cosplay, zona de consolas, tiendas y firma de autografos.",
                    List.of("Productora Nerd Fest", "Comunidad Cosplay Chile"),
                    List.of("Cosplayer Fernanda Lagos", "Ilustrador Tomas Herrera"),
                    true, true, true, true, true),
            new Evento(6L, "Bootcamp de Desarrollo de Videojuegos", "Taller",
                    LocalDate.of(2026, 12, 19), LocalTime.of(9, 30), "Valparaiso",
                    "Av. Espana 1680, Valparaiso",
                    "Taller intensivo de un dia para crear tu primer juego en Godot, desde cero hasta publicarlo.",
                    List.of("Game Dev Valpo"),
                    List.of("Instructora Paula Mendez"),
                    false, true, true, true, false)
    );

    public List<Evento> buscar(String categoria, LocalDate fecha, String ubicacion) {
        return eventos.stream()
                .filter(e -> categoria == null || categoria.isBlank() || e.getCategoria().equalsIgnoreCase(categoria))
                .filter(e -> fecha == null || e.getFecha().equals(fecha))
                .filter(e -> ubicacion == null || ubicacion.isBlank() || e.getUbicacion().equalsIgnoreCase(ubicacion))
                .sorted(Comparator.comparing(Evento::getFecha))
                .toList();
    }

    public Optional<Evento> obtenerPorId(Long id) {
        return eventos.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public List<Evento> proximos(int cantidad) {
        LocalDate hoy = LocalDate.now();
        return eventos.stream()
                .filter(e -> !e.getFecha().isBefore(hoy))
                .sorted(Comparator.comparing(Evento::getFecha))
                .limit(cantidad)
                .toList();
    }

    public List<Evento> destacados() {
        return eventos.stream().filter(Evento::isDestacado).toList();
    }

    public List<String> categorias() {
        return eventos.stream().map(Evento::getCategoria).distinct().sorted().toList();
    }

    public List<String> ubicaciones() {
        return eventos.stream().map(Evento::getUbicacion).distinct().sorted().toList();
    }
}
