package com.duoc.gamer_events_web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Evento tal como lo entrega la API del backend
@Getter
@Setter
@NoArgsConstructor
public class Evento {

    private Long id;
    private String nombre;
    private String categoria;
    private LocalDate fecha;
    private LocalTime hora;
    private String ubicacion;
    private String direccion;
    private String descripcion;
    private List<String> organizadores;
    private List<String> expositores;
    private boolean estacionamiento;
    private boolean locomocionColectiva;
    private boolean servicioComidas;
    private boolean alojamiento;
    private boolean destacado;
}
