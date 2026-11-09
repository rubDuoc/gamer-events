package com.duoc.gamer_events_web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Perfil {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Solo letras, numeros y guion bajo")
    private String nombreUsuario;

    private String avatar;

    private List<String> juegos = new ArrayList<>();

    private boolean notificarPorEmail;
    private boolean notificarNuevosEventos;
    private boolean notificarRecordatorios;
}
