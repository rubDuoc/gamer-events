package com.duoc.gamer_events.config;

import com.duoc.gamer_events.model.Evento;
import com.duoc.gamer_events.model.Marca;
import com.duoc.gamer_events.model.Rol;
import com.duoc.gamer_events.repository.EventoRepository;
import com.duoc.gamer_events.repository.MarcaRepository;
import com.duoc.gamer_events.repository.UsuarioRepository;
import com.duoc.gamer_events.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Carga los datos de ejemplo en la base de datos la primera vez que se inicia la aplicacion
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EventoRepository eventoRepository;
    private final MarcaRepository marcaRepository;
    private final UsuarioService usuarioService;

    public DatosIniciales(UsuarioRepository usuarioRepository, EventoRepository eventoRepository,
                          MarcaRepository marcaRepository, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
        this.marcaRepository = marcaRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.saveAll(List.of(
                    usuarioService.nuevoUsuario("Jugador Uno", "jugador1@gamerevents.cl", "Jugador1#2026", Rol.USER),
                    usuarioService.nuevoUsuario("Jugador Dos", "jugador2@gamerevents.cl", "Jugador2#2026", Rol.USER),
                    usuarioService.nuevoUsuario("Administrador", "admin@gamerevents.cl", "Admin#2026", Rol.ADMIN)));
        }
        if (marcaRepository.count() == 0) {
            marcaRepository.saveAll(List.of(
                    new Marca("PixelForge Perifericos"), new Marca("NovaByte Energy Drink"),
                    new Marca("Titan Sillas Gamer"), new Marca("CloudPlay Streaming")));
        }
        if (eventoRepository.count() == 0) {
            eventoRepository.saveAll(eventos());
        }
    }

    private List<Evento> eventos() {
        return List.of(
                new Evento(null, "Torneo Regional de League of Legends", "Torneo",
                        LocalDate.of(2026, 11, 14), LocalTime.of(10, 0), "Santiago",
                        "Av. Libertador Bernardo O'Higgins 3363, Estacion Central",
                        "Torneo 5v5 abierto a equipos amateur. Fase de grupos en la manana y playoffs en la tarde.",
                        List.of("Liga Gamer Chile", "Duoc UC Esports"),
                        List.of("Caster Nicolas 'Nikko' Rojas", "Coach Valentina Paredes"),
                        true, true, true, false, true),
                new Evento(null, "Expo Indie Games Valparaiso", "Exposicion",
                        LocalDate.of(2026, 11, 21), LocalTime.of(11, 0), "Valparaiso",
                        "Av. Brasil 2950, Valparaiso",
                        "Muestra de videojuegos independientes chilenos con zona de prueba y charlas de desarrolladores.",
                        List.of("Asociacion de Desarrolladores Indie"),
                        List.of("Estudio Pixel Austral", "Estudio Cordillera Games"),
                        false, true, true, false, true),
                new Evento(null, "LAN Party Retro", "LAN Party",
                        LocalDate.of(2026, 11, 28), LocalTime.of(18, 0), "Concepcion",
                        "Barros Arana 1068, Concepcion",
                        "Noche de juegos clasicos en red: Counter-Strike 1.6, Age of Empires II y Quake III.",
                        List.of("Club Retro Biobio"),
                        List.of("Coleccionista Andres Fuentes"),
                        true, true, true, false, false),
                new Evento(null, "Copa Speedrun Chile", "Torneo",
                        LocalDate.of(2026, 12, 5), LocalTime.of(15, 0), "Santiago",
                        "Av. Providencia 1308, Providencia",
                        "Competencia de speedrun en vivo con categorias Any% y 100% en juegos de plataformas.",
                        List.of("Speedrun Chile"),
                        List.of("Runner Camila 'Kami' Soto", "Runner Diego Vera"),
                        false, true, false, false, false),
                new Evento(null, "Feria Cosplay & Gaming", "Convencion",
                        LocalDate.of(2026, 12, 12), LocalTime.of(12, 0), "Santiago",
                        "Av. Kennedy 9001, Las Condes",
                        "Convencion con concurso de cosplay, zona de consolas, tiendas y firma de autografos.",
                        List.of("Productora Nerd Fest", "Comunidad Cosplay Chile"),
                        List.of("Cosplayer Fernanda Lagos", "Ilustrador Tomas Herrera"),
                        true, true, true, true, true),
                new Evento(null, "Bootcamp de Desarrollo de Videojuegos", "Taller",
                        LocalDate.of(2026, 12, 19), LocalTime.of(9, 30), "Valparaiso",
                        "Av. Espana 1680, Valparaiso",
                        "Taller intensivo de un dia para crear tu primer juego en Godot, desde cero hasta publicarlo.",
                        List.of("Game Dev Valpo"),
                        List.of("Instructora Paula Mendez"),
                        false, true, true, true, false));
    }
}
