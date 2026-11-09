package com.duoc.gamer_events_web.controller;

import com.duoc.gamer_events_web.client.BackendClient;
import com.duoc.gamer_events_web.client.Respuestas;
import com.duoc.gamer_events_web.model.Perfil;
import com.duoc.gamer_events_web.security.UsuarioSesion;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PerfilController {

    private final BackendClient backendClient;

    public PerfilController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    @GetMapping("/perfil")
    public String verPerfil(@AuthenticationPrincipal UsuarioSesion usuario, Model model) {
        model.addAttribute("perfil", backendClient.perfil(usuario.token()));
        cargarOpciones(usuario, model);
        return "perfil";
    }

    // El backend guarda el perfil del usuario dueño del token, nunca el de otro usuario
    @PostMapping("/perfil")
    public String guardarPerfil(@Valid @ModelAttribute Perfil perfil, BindingResult result,
                                @AuthenticationPrincipal UsuarioSesion usuario, Model model) {
        if (result.hasErrors()) {
            cargarOpciones(usuario, model);
            return "perfil";
        }
        backendClient.guardarPerfil(usuario.token(), perfil);
        return "redirect:/perfil?guardado";
    }

    private void cargarOpciones(UsuarioSesion usuario, Model model) {
        Respuestas.OpcionesPerfil opciones = backendClient.opcionesPerfil(usuario.token());
        model.addAttribute("nombre", usuario.nombre());
        model.addAttribute("juegosDisponibles", opciones.juegos());
        model.addAttribute("avatares", opciones.avatares());
    }
}
