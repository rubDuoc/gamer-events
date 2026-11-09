package com.duoc.gamer_events_web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

// Si el backend rechaza el token (expirado o invalido) se cierra la sesion y se pide iniciar sesion de nuevo
@ControllerAdvice
public class SesionExpiradaHandler {

    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public String tokenRechazado(HttpServletRequest request) throws ServletException {
        request.logout();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        return "redirect:/login?expirada";
    }
}
