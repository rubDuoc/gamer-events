package com.duoc.gamer_events.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

// Registro de cada vez que un usuario comparte un evento, en el sitio o en una red social
@Entity
@Table(name = "compartidos")
@Getter
@NoArgsConstructor
public class Compartido {

    // SITIO: aparece en la seccion "Compartidos por la comunidad" del inicio
    public enum Red { SITIO, X, FACEBOOK, WHATSAPP }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Evento evento;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Red red;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public Compartido(Evento evento, Usuario usuario, Red red) {
        this.evento = evento;
        this.usuario = usuario;
        this.red = red;
        this.fecha = LocalDateTime.now();
    }
}
