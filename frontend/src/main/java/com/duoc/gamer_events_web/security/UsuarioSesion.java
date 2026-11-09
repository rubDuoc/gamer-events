package com.duoc.gamer_events_web.security;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serializable;

// Usuario autenticado. Se guarda en la sesion HTTP del servidor (nunca en el navegador),
// asi cada usuario tiene su propio token JWT y este no queda compartido entre usuarios.
public record UsuarioSesion(String email, String nombre, String token) implements AuthenticatedPrincipal, Serializable {

    @Override
    public String getName() {
        return email;
    }

    // Evita que el token aparezca en los logs
    @Override
    public String toString() {
        return "UsuarioSesion[email=" + email + "]";
    }
}
