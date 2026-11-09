package com.duoc.gamer_events.repository;

import com.duoc.gamer_events.model.Evento;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    // Los filtros nulos se ignoran; los parametros van enlazados, nunca concatenados (evita inyeccion SQL)
    @Query("""
            select e from Evento e
            where (:categoria is null or lower(e.categoria) = lower(:categoria))
              and (:fecha is null or e.fecha = :fecha)
              and (:ubicacion is null or lower(e.ubicacion) = lower(:ubicacion))
            order by e.fecha
            """)
    List<Evento> buscar(@Param("categoria") String categoria,
                        @Param("fecha") LocalDate fecha,
                        @Param("ubicacion") String ubicacion);

    List<Evento> findByFechaGreaterThanEqualOrderByFechaAsc(LocalDate fecha, Limit limit);

    List<Evento> findByDestacadoTrueOrderByFechaAsc();

    @Query("select distinct e.categoria from Evento e order by e.categoria")
    List<String> categorias();

    @Query("select distinct e.ubicacion from Evento e order by e.ubicacion")
    List<String> ubicaciones();
}
