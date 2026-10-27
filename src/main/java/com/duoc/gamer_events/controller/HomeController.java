package com.duoc.gamer_events.controller;

import com.duoc.gamer_events.service.EventoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private static final List<String> MARCAS = List.of(
            "PixelForge Perifericos", "NovaByte Energy Drink", "Titan Sillas Gamer", "CloudPlay Streaming");

    private final EventoService eventoService;

    public HomeController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("proximos", eventoService.proximos(3));
        model.addAttribute("destacados", eventoService.destacados());
        model.addAttribute("marcas", MARCAS);
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
