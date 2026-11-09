package com.duoc.gamer_events_web.controller;

import com.duoc.gamer_events_web.client.BackendClient;
import com.duoc.gamer_events_web.model.RegistroForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;

@Controller
public class RegistroController {

    private final BackendClient backendClient;

    public RegistroController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    @GetMapping("/registro")
    public String formulario(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    // Se valida en el frontend y otra vez en el backend, que es quien crea la cuenta
    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute RegistroForm registroForm, BindingResult result) {
        if (result.hasErrors()) {
            return "registro";
        }
        try {
            backendClient.registrar(registroForm);
        } catch (HttpClientErrorException.Conflict e) {
            result.rejectValue("email", "duplicado", "Ya existe una cuenta con este correo");
            return "registro";
        }
        return "redirect:/login?registrado";
    }
}
