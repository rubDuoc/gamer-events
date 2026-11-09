package com.duoc.gamer_events_web.controller;

import com.duoc.gamer_events_web.client.BackendClient;
import com.duoc.gamer_events_web.client.Respuestas;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final BackendClient backendClient;

    public HomeController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    // Pagina publica: usa la API publica /api/inicio (sin token)
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        Respuestas.Inicio inicio = backendClient.inicio();
        model.addAttribute("proximos", inicio.proximos());
        model.addAttribute("destacados", inicio.destacados());
        model.addAttribute("marcas", inicio.marcas());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
