package com.duoc.gamer_events.repository;

import com.duoc.gamer_events.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    Optional<Valoracion> findByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);

    long countByEventoId(Long eventoId);

    @Query("select avg(v.puntaje) from Valoracion v where v.evento.id = :eventoId")
    Double promedio(@Param("eventoId") Long eventoId);
}
