-- =====================================================================
-- Gamer Events: creacion de la base de datos y datos iniciales (MySQL 8)
-- Las contraseñas se guardan cifradas con BCrypt, nunca en texto plano.
-- Usuarios de prueba:
--   jugador1@gamerevents.cl / Jugador1#2026  (USER)
--   jugador2@gamerevents.cl / Jugador2#2026  (USER)
--   admin@gamerevents.cl    / Admin#2026     (ADMIN)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS gamer_events CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE gamer_events;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- Tablas
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS usuarios (
    id                       BIGINT       NOT NULL AUTO_INCREMENT,
    nombre                   VARCHAR(100) NOT NULL,
    email                    VARCHAR(150) NOT NULL,
    password                 VARCHAR(100) NOT NULL,
    rol                      ENUM('ADMIN','USER') NOT NULL,
    nombre_usuario           VARCHAR(30)  DEFAULT NULL,
    avatar                   VARCHAR(10)  DEFAULT NULL,
    notificar_por_email      BIT(1)       NOT NULL,
    notificar_nuevos_eventos BIT(1)       NOT NULL,
    notificar_recordatorios  BIT(1)       NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS usuario_juegos (
    usuario_id BIGINT      NOT NULL,
    juego      VARCHAR(50) DEFAULT NULL,
    KEY fk_usuario_juegos_usuario (usuario_id),
    CONSTRAINT fk_usuario_juegos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS marcas (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_marcas_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS eventos (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    nombre               VARCHAR(150) NOT NULL,
    categoria            VARCHAR(50)  NOT NULL,
    fecha                DATE         NOT NULL,
    hora                 TIME         NOT NULL,
    ubicacion            VARCHAR(50)  NOT NULL,
    direccion            VARCHAR(200) NOT NULL,
    descripcion          VARCHAR(500) NOT NULL,
    estacionamiento      BIT(1)       NOT NULL,
    locomocion_colectiva BIT(1)       NOT NULL,
    servicio_comidas     BIT(1)       NOT NULL,
    alojamiento          BIT(1)       NOT NULL,
    destacado            BIT(1)       NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS evento_organizadores (
    evento_id   BIGINT       NOT NULL,
    orden       INT          NOT NULL,
    organizador VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (evento_id, orden),
    CONSTRAINT fk_evento_organizadores_evento FOREIGN KEY (evento_id) REFERENCES eventos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS evento_expositores (
    evento_id BIGINT       NOT NULL,
    orden     INT          NOT NULL,
    expositor VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (evento_id, orden),
    CONSTRAINT fk_evento_expositores_evento FOREIGN KEY (evento_id) REFERENCES eventos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Datos iniciales
-- ---------------------------------------------------------------------

INSERT INTO usuarios (id, nombre, email, password, rol, nombre_usuario, avatar,
                      notificar_por_email, notificar_nuevos_eventos, notificar_recordatorios) VALUES
(1, 'Jugador Uno',   'jugador1@gamerevents.cl', '$2a$10$L7gWF0.2XTY236XtRFccKuuWaC4E/9oDDPkDWP3Gpf4caD.nANcqq', 'USER',  NULL, '🎮', 1, 0, 0),
(2, 'Jugador Dos',   'jugador2@gamerevents.cl', '$2a$10$6CTDzpRMt0Z464aXsZqdyeP.rAcXcLK9VyNyjdqnDMoqqK1ya12YW', 'USER',  NULL, '🎮', 1, 0, 0),
(3, 'Administrador', 'admin@gamerevents.cl',    '$2a$10$A2/4Vi7BvHDkXxwLLSiqp.xcdMg0b9WdUHWQYSV24NXxjSbJSiJ3S', 'ADMIN', NULL, '🎮', 1, 0, 0);

INSERT INTO marcas (id, nombre) VALUES
(1, 'PixelForge Perifericos'),
(2, 'NovaByte Energy Drink'),
(3, 'Titan Sillas Gamer'),
(4, 'CloudPlay Streaming');

INSERT INTO eventos (id, nombre, categoria, fecha, hora, ubicacion, direccion, descripcion,
                     estacionamiento, locomocion_colectiva, servicio_comidas, alojamiento, destacado) VALUES
(1, 'Torneo Regional de League of Legends', 'Torneo', '2026-11-14', '10:00:00', 'Santiago',
    'Av. Libertador Bernardo O''Higgins 3363, Estacion Central',
    'Torneo 5v5 abierto a equipos amateur. Fase de grupos en la manana y playoffs en la tarde.', 1, 1, 1, 0, 1),
(2, 'Expo Indie Games Valparaiso', 'Exposicion', '2026-11-21', '11:00:00', 'Valparaiso',
    'Av. Brasil 2950, Valparaiso',
    'Muestra de videojuegos independientes chilenos con zona de prueba y charlas de desarrolladores.', 0, 1, 1, 0, 1),
(3, 'LAN Party Retro', 'LAN Party', '2026-11-28', '18:00:00', 'Concepcion',
    'Barros Arana 1068, Concepcion',
    'Noche de juegos clasicos en red: Counter-Strike 1.6, Age of Empires II y Quake III.', 1, 1, 1, 0, 0),
(4, 'Copa Speedrun Chile', 'Torneo', '2026-12-05', '15:00:00', 'Santiago',
    'Av. Providencia 1308, Providencia',
    'Competencia de speedrun en vivo con categorias Any% y 100% en juegos de plataformas.', 0, 1, 0, 0, 0),
(5, 'Feria Cosplay & Gaming', 'Convencion', '2026-12-12', '12:00:00', 'Santiago',
    'Av. Kennedy 9001, Las Condes',
    'Convencion con concurso de cosplay, zona de consolas, tiendas y firma de autografos.', 1, 1, 1, 1, 1),
(6, 'Bootcamp de Desarrollo de Videojuegos', 'Taller', '2026-12-19', '09:30:00', 'Valparaiso',
    'Av. Espana 1680, Valparaiso',
    'Taller intensivo de un dia para crear tu primer juego en Godot, desde cero hasta publicarlo.', 0, 1, 1, 1, 0);

INSERT INTO evento_organizadores (evento_id, orden, organizador) VALUES
(1, 0, 'Liga Gamer Chile'), (1, 1, 'Duoc UC Esports'),
(2, 0, 'Asociacion de Desarrolladores Indie'),
(3, 0, 'Club Retro Biobio'),
(4, 0, 'Speedrun Chile'),
(5, 0, 'Productora Nerd Fest'), (5, 1, 'Comunidad Cosplay Chile'),
(6, 0, 'Game Dev Valpo');

INSERT INTO evento_expositores (evento_id, orden, expositor) VALUES
(1, 0, 'Caster Nicolas ''Nikko'' Rojas'), (1, 1, 'Coach Valentina Paredes'),
(2, 0, 'Estudio Pixel Austral'), (2, 1, 'Estudio Cordillera Games'),
(3, 0, 'Coleccionista Andres Fuentes'),
(4, 0, 'Runner Camila ''Kami'' Soto'), (4, 1, 'Runner Diego Vera'),
(5, 0, 'Cosplayer Fernanda Lagos'), (5, 1, 'Ilustrador Tomas Herrera'),
(6, 0, 'Instructora Paula Mendez');
