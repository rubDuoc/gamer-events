package com.duoc.gamer_events.controller;

import com.duoc.gamer_events.model.Perfil;
import com.duoc.gamer_events.service.PerfilService;
import com.duoc.gamer_events.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PerfilController {

    private final PerfilService perfilService;
    private final UsuarioService usuarioService;

    public PerfilController(PerfilService perfilService, UsuarioService usuarioService) {
        this.perfilService = perfilService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil")
    public String verPerfil(Authentication auth, Model model) {
        model.addAttribute("perfil", perfilService.obtener(auth.getName()));
        cargarOpciones(auth, model);
        return "perfil";
    }

    // El perfil siempre se guarda para el usuario autenticado, nunca para uno recibido en el formulario
    @PostMapping("/perfil")
    public String guardarPerfil(@Valid @ModelAttribute Perfil perfil, BindingResult result,
                                Authentication auth, Model model) {
        if (result.hasErrors()) {
            cargarOpciones(auth, model);
            return "perfil";
        }
        perfilService.guardar(auth.getName(), perfil);
        return "redirect:/perfil?guardado";
    }

    private void cargarOpciones(Authentication auth, Model model) {
        model.addAttribute("nombre", usuarioService.obtenerNombre(auth.getName()));
        model.addAttribute("juegosDisponibles", PerfilService.JUEGOS);
        model.addAttribute("avatares", PerfilService.AVATARES);
    }
}
