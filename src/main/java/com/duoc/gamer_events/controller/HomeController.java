package com.duoc.gamer_events.controller;

import com.duoc.gamer_events.service.EventoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final EventoService eventoService;

    public HomeController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("proximos", eventoService.proximos(3));
        model.addAttribute("destacados", eventoService.destacados());
        model.addAttribute("marcas", eventoService.marcas());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
