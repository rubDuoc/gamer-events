package com.duoc.gamer_events.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

// Foto o video de un evento. Las fotos se guardan en la BD (ya validadas); de los videos solo se guarda
// el identificador de YouTube, nunca una URL libre (asi no se pueden incrustar sitios arbitrarios)
@Entity
@Table(name = "medias")
@Getter
@Setter
@NoArgsConstructor
public class Media {

    public enum Tipo { FOTO, VIDEO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Evento evento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Tipo tipo;

    // Solo FOTO: contenido y tipo MIME verificado
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @Column(length = 30)
    private String contentType;

    // Solo VIDEO: id de 11 caracteres de YouTube
    @Column(length = 11)
    private String videoId;

    @Column(nullable = false, length = 150)
    private String autorEmail;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
