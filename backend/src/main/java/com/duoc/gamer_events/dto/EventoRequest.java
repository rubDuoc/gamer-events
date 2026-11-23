package com.duoc.gamer_events.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Datos para publicar un evento. Todo se valida en el servidor, aunque el formulario tambien valide
public record EventoRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 5, max = 150, message = "El nombre debe tener entre 5 y 150 caracteres")
        String nombre,

        @NotBlank(message = "La categoria es obligatoria")
        @Pattern(regexp = "Torneo|Exposicion|LAN Party|Convencion|Taller", message = "Categoria no valida")
        String categoria,

        @NotNull(message = "La fecha es obligatoria")
        @FutureOrPresent(message = "La fecha no puede ser pasada")
        LocalDate fecha,

        @NotNull(message = "La hora es obligatoria")
        LocalTime hora,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(min = 3, max = 50, message = "La ciudad debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "La ciudad solo puede tener letras y espacios")
        String ubicacion,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(min = 5, max = 200, message = "La direccion debe tener entre 5 y 200 caracteres")
        String direccion,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(min = 20, max = 500, message = "La descripcion debe tener entre 20 y 500 caracteres")
        String descripcion,

        @Size(max = 5, message = "Maximo 5 organizadores")
        List<@NotBlank @Size(max = 100, message = "Cada organizador puede tener hasta 100 caracteres") String> organizadores,

        @Size(max = 5, message = "Maximo 5 expositores")
        List<@NotBlank @Size(max = 100, message = "Cada expositor puede tener hasta 100 caracteres") String> expositores,

        // Servicios opcionales: si no se envian, se consideran no disponibles
        Boolean estacionamiento,
        Boolean locomocionColectiva,
        Boolean servicioComidas,
        Boolean alojamiento
) {
}
