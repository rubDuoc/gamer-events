package com.duoc.gamer_events.service;

import com.duoc.gamer_events.model.Perfil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PerfilService {

    public static final List<String> JUEGOS = List.of(
            "League of Legends", "Valorant", "Counter-Strike 2", "Fortnite",
            "Minecraft", "EA Sports FC", "Street Fighter 6", "Rocket League");

    public static final List<String> AVATARES = List.of("🎮", "👾", "🕹️", "🐉", "🚀", "🦊");

    // Perfiles en memoria, uno por usuario autenticado
    private final Map<String, Perfil> perfiles = new ConcurrentHashMap<>();

    public Perfil obtener(String username) {
        return perfiles.getOrDefault(username, perfilPorDefecto());
    }

    // Solo se aceptan juegos y avatares de las listas permitidas
    public void guardar(String username, Perfil perfil) {
        List<String> juegos = perfil.getJuegos() == null ? List.of() : perfil.getJuegos();
        perfil.setJuegos(new ArrayList<>(juegos.stream().filter(JUEGOS::contains).toList()));
        if (!AVATARES.contains(perfil.getAvatar())) {
            perfil.setAvatar(AVATARES.get(0));
        }
        perfiles.put(username, perfil);
    }

    private Perfil perfilPorDefecto() {
        Perfil perfil = new Perfil();
        perfil.setAvatar(AVATARES.get(0));
        perfil.setNotificarPorEmail(true);
        return perfil;
    }
}
