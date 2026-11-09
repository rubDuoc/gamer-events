package com.duoc.gamer_events.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Hash BCrypt, nunca la contraseña en texto plano
    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    // Datos del perfil gamer
    @Column(length = 30)
    private String nombreUsuario;

    @Column(length = 10)
    private String avatar;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_juegos", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "juego", length = 50)
    private List<String> juegos = new ArrayList<>();

    private boolean notificarPorEmail;
    private boolean notificarNuevosEventos;
    private boolean notificarRecordatorios;

    public Usuario(String nombre, String email, String password, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }
}
