package com.duoc.gamer_events.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "eventos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 50)
    private String ubicacion;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evento_organizadores", joinColumns = @JoinColumn(name = "evento_id"))
    @OrderColumn(name = "orden")
    @Column(name = "organizador", length = 100)
    private List<String> organizadores;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evento_expositores", joinColumns = @JoinColumn(name = "evento_id"))
    @OrderColumn(name = "orden")
    @Column(name = "expositor", length = 100)
    private List<String> expositores;

    private boolean estacionamiento;
    private boolean locomocionColectiva;
    private boolean servicioComidas;
    private boolean alojamiento;
    private boolean destacado;
}
