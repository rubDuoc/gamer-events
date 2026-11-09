package com.duoc.gamer_events.service;

import com.duoc.gamer_events.model.Perfil;
import com.duoc.gamer_events.model.Usuario;
import com.duoc.gamer_events.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PerfilService {

    public static final List<String> JUEGOS = List.of(
            "League of Legends", "Valorant", "Counter-Strike 2", "Fortnite",
            "Minecraft", "EA Sports FC", "Street Fighter 6", "Rocket League");

    public static final List<String> AVATARES = List.of("🎮", "👾", "🕹️", "🐉", "🚀", "🦊");

    private final UsuarioRepository usuarioRepository;

    public PerfilService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // El perfil se guarda en la tabla de usuarios, asociado al usuario autenticado
    public Perfil obtener(String email) {
        Usuario usuario = buscarUsuario(email);
        Perfil perfil = new Perfil();
        perfil.setNombreUsuario(usuario.getNombreUsuario());
        perfil.setAvatar(AVATARES.contains(usuario.getAvatar()) ? usuario.getAvatar() : AVATARES.get(0));
        perfil.setJuegos(new ArrayList<>(usuario.getJuegos()));
        perfil.setNotificarPorEmail(usuario.isNotificarPorEmail());
        perfil.setNotificarNuevosEventos(usuario.isNotificarNuevosEventos());
        perfil.setNotificarRecordatorios(usuario.isNotificarRecordatorios());
        return perfil;
    }

    // Solo se aceptan juegos y avatares de las listas permitidas
    public void guardar(String email, Perfil perfil) {
        Usuario usuario = buscarUsuario(email);
        List<String> juegos = perfil.getJuegos() == null ? List.of() : perfil.getJuegos();
        usuario.setNombreUsuario(perfil.getNombreUsuario());
        usuario.setAvatar(AVATARES.contains(perfil.getAvatar()) ? perfil.getAvatar() : AVATARES.get(0));
        usuario.setJuegos(new ArrayList<>(juegos.stream().filter(JUEGOS::contains).distinct().toList()));
        usuario.setNotificarPorEmail(perfil.isNotificarPorEmail());
        usuario.setNotificarNuevosEventos(perfil.isNotificarNuevosEventos());
        usuario.setNotificarRecordatorios(perfil.isNotificarRecordatorios());
        usuarioRepository.save(usuario);
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
