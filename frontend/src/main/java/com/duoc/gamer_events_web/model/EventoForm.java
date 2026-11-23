package com.duoc.gamer_events_web.model;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Formulario para publicar un evento. Se valida aqui y nuevamente en el backend
@Getter
@Setter
@NoArgsConstructor
public class EventoForm {

    public static final List<String> CATEGORIAS = List.of("Torneo", "Exposicion", "LAN Party", "Convencion", "Taller");

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 5, max = 150, message = "El nombre debe tener entre 5 y 150 caracteres")
    private String nombre;

    @NotBlank(message = "Selecciona una categoria")
    @Pattern(regexp = "Torneo|Exposicion|LAN Party|Convencion|Taller", message = "Categoria no valida")
    private String categoria;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser pasada")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(min = 3, max = 50, message = "La ciudad debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L} ]*$", message = "La ciudad solo puede tener letras y espacios")
    private String ubicacion;

    @NotBlank(message = "La direccion es obligatoria")
    @Size(min = 5, max = 200, message = "La direccion debe tener entre 5 y 200 caracteres")
    private String direccion;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(min = 20, max = 500, message = "La descripcion debe tener entre 20 y 500 caracteres")
    private String descripcion;

    // Separados por coma
    @Size(max = 300, message = "Maximo 300 caracteres")
    private String organizadores;

    @Size(max = 300, message = "Maximo 300 caracteres")
    private String expositores;

    private boolean estacionamiento;
    private boolean locomocionColectiva;
    private boolean servicioComidas;
    private boolean alojamiento;

    // Cuerpo JSON que espera POST /api/eventos
    public Map<String, Object> aPeticion() {
        Map<String, Object> peticion = new LinkedHashMap<>();
        peticion.put("nombre", nombre);
        peticion.put("categoria", categoria);
        peticion.put("fecha", fecha == null ? null : fecha.toString());
        peticion.put("hora", hora == null ? null : hora.toString());
        peticion.put("ubicacion", ubicacion);
        peticion.put("direccion", direccion);
        peticion.put("descripcion", descripcion);
        peticion.put("organizadores", lista(organizadores));
        peticion.put("expositores", lista(expositores));
        peticion.put("estacionamiento", estacionamiento);
        peticion.put("locomocionColectiva", locomocionColectiva);
        peticion.put("servicioComidas", servicioComidas);
        peticion.put("alojamiento", alojamiento);
        return peticion;
    }

    private static List<String> lista(String texto) {
        return texto == null ? List.of() : Arrays.stream(texto.split(",")).map(String::trim).filter(v -> !v.isEmpty()).toList();
    }
}
