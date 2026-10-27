package com.duoc.gamer_events.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
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
