package com.duoc.gamer_events.repository;

import com.duoc.gamer_events.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findByEventoIdOrderByFechaAsc(Long eventoId);

    long countByEventoIdAndTipo(Long eventoId, Media.Tipo tipo);
}
