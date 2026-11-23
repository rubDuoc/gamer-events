package com.duoc.gamer_events.repository;

import com.duoc.gamer_events.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByEventoIdOrderByFechaAsc(Long eventoId);
}
