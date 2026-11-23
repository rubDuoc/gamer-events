package com.duoc.gamer_events.repository;

import com.duoc.gamer_events.model.Compartido;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompartidoRepository extends JpaRepository<Compartido, Long> {

    List<Compartido> findByRedOrderByFechaDesc(Compartido.Red red, Limit limit);

    long countByEventoId(Long eventoId);
}
