package com.duoc.gamer_events.controller.api;

import com.duoc.gamer_events.dto.UsuarioResponse;
import com.duoc.gamer_events.service.UsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// [Privada, solo rol ADMIN] protegida en WebSecurityConfig con hasRole("ADMIN")
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final UsuarioService usuarioService;

    public AdminApiController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> usuarios() {
        return usuarioService.listar().stream().map(UsuarioResponse::from).toList();
    }
}
