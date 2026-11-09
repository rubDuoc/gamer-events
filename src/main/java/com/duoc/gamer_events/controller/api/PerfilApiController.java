package com.duoc.gamer_events.controller.api;

import com.duoc.gamer_events.model.Perfil;
import com.duoc.gamer_events.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// [Privada] el perfil siempre es el del usuario dueño del token, nunca uno recibido como parametro
@RestController
@RequestMapping("/api/perfil")
public class PerfilApiController {

    private final PerfilService perfilService;

    public PerfilApiController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public Perfil obtener(Authentication auth) {
        return perfilService.obtener(auth.getName());
    }

    @PutMapping
    public Perfil actualizar(@Valid @RequestBody Perfil perfil, Authentication auth) {
        perfilService.guardar(auth.getName(), perfil);
        return perfilService.obtener(auth.getName());
    }
}
