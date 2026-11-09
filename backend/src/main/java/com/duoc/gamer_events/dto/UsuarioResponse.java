package com.duoc.gamer_events.dto;

import com.duoc.gamer_events.model.Rol;
import com.duoc.gamer_events.model.Usuario;

// Datos publicos de un usuario: nunca se expone la contraseña
public record UsuarioResponse(Long id, String nombre, String email, Rol rol) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}
