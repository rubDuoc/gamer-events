package com.duoc.gamer_events.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

// Valoracion de 1 a 5 estrellas: cada usuario tiene una sola por evento (si vuelve a valorar, se actualiza)
@Entity
@Table(name = "valoraciones", uniqueConstraints = @UniqueConstraint(columnNames = {"evento_id", "usuario_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @Column(nullable = false)
    private int puntaje;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
