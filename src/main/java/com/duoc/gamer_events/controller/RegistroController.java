package com.duoc.gamer_events.controller;

import com.duoc.gamer_events.model.RegistroForm;
import com.duoc.gamer_events.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistroController {

    private final UsuarioService usuarioService;

    public RegistroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String formulario(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute RegistroForm registroForm, BindingResult result) {
        if (!result.hasFieldErrors("email") && usuarioService.existe(registroForm.getEmail())) {
            result.rejectValue("email", "duplicado", "Ya existe una cuenta con este correo");
        }
        if (result.hasErrors()) {
            return "registro";
        }
        usuarioService.registrar(registroForm);
        return "redirect:/login?registrado";
    }
}
