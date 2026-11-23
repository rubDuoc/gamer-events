package com.duoc.gamer_events.service;

import com.duoc.gamer_events.dto.ComunidadDtos.CompartidoResponse;
import com.duoc.gamer_events.dto.ComunidadDtos.ComentarioResponse;
import com.duoc.gamer_events.dto.ComunidadDtos.ValoracionResponse;
import com.duoc.gamer_events.model.Comentario;
import com.duoc.gamer_events.model.Compartido;
import com.duoc.gamer_events.model.Evento;
import com.duoc.gamer_events.model.Usuario;
import com.duoc.gamer_events.model.Valoracion;
import com.duoc.gamer_events.repository.ComentarioRepository;
import com.duoc.gamer_events.repository.CompartidoRepository;
import com.duoc.gamer_events.repository.EventoRepository;
import com.duoc.gamer_events.repository.UsuarioRepository;
import com.duoc.gamer_events.repository.ValoracionRepository;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

// Comentarios, valoraciones y compartidos de los eventos. El usuario siempre es el del token JWT
@Service
@Transactional
public class ComunidadService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComentarioRepository comentarioRepository;
    private final ValoracionRepository valoracionRepository;
    private final CompartidoRepository compartidoRepository;

    public ComunidadService(EventoRepository eventoRepository, UsuarioRepository usuarioRepository,
                            ComentarioRepository comentarioRepository, ValoracionRepository valoracionRepository,
                            CompartidoRepository compartidoRepository) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.comentarioRepository = comentarioRepository;
        this.valoracionRepository = valoracionRepository;
        this.compartidoRepository = compartidoRepository;
    }

    // ---------------- Comentarios
    @Transactional(readOnly = true)
    public List<ComentarioResponse> comentarios(Long eventoId) {
        buscarEvento(eventoId);
        return comentarioRepository.findByEventoIdOrderByFechaAsc(eventoId).stream().map(ComentarioResponse::from).toList();
    }

    public ComentarioResponse comentar(Long eventoId, String texto, String email) {
        Comentario comentario = new Comentario(buscarEvento(eventoId), buscarUsuario(email), texto.trim());
        return ComentarioResponse.from(comentarioRepository.save(comentario));
    }

    // Solo el autor del comentario o un ADMIN puede eliminarlo
    public void eliminarComentario(Long comentarioId, String email, boolean esAdmin) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));
        if (!esAdmin && !comentario.getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes eliminar tus propios comentarios");
        }
        comentarioRepository.delete(comentario);
    }

    // ---------------- Valoraciones (1 a 5; una por usuario y evento)
    @Transactional(readOnly = true)
    public ValoracionResponse resumenValoracion(Long eventoId, String email) {
        buscarEvento(eventoId);
        Usuario usuario = buscarUsuario(email);
        Double promedio = valoracionRepository.promedio(eventoId);
        Integer miPuntaje = valoracionRepository.findByEventoIdAndUsuarioId(eventoId, usuario.getId())
                .map(Valoracion::getPuntaje).orElse(null);
        return new ValoracionResponse(promedio == null ? 0 : Math.round(promedio * 10) / 10.0,
                valoracionRepository.countByEventoId(eventoId), miPuntaje);
    }

    public ValoracionResponse valorar(Long eventoId, int puntaje, String email) {
        Evento evento = buscarEvento(eventoId);
        Usuario usuario = buscarUsuario(email);
        Valoracion valoracion = valoracionRepository.findByEventoIdAndUsuarioId(eventoId, usuario.getId())
                .orElseGet(Valoracion::new);
        valoracion.setEvento(evento);
        valoracion.setUsuario(usuario);
        valoracion.setPuntaje(puntaje);
        valoracion.setFecha(LocalDateTime.now());
        valoracionRepository.saveAndFlush(valoracion);
        return resumenValoracion(eventoId, email);
    }

    // ---------------- Compartir en el sitio o en redes sociales
    public CompartidoResponse compartir(Long eventoId, Compartido.Red red, String email) {
        Compartido compartido = new Compartido(buscarEvento(eventoId), buscarUsuario(email), red);
        return CompartidoResponse.from(compartidoRepository.save(compartido));
    }

    @Transactional(readOnly = true)
    public long vecesCompartido(Long eventoId) {
        return compartidoRepository.countByEventoId(eventoId);
    }

    // Publico: lo que la comunidad compartio en el sitio (se muestra en el inicio)
    @Transactional(readOnly = true)
    public List<CompartidoResponse> compartidosEnElSitio(int cantidad) {
        return compartidoRepository.findByRedOrderByFechaDesc(Compartido.Red.SITIO, Limit.of(cantidad)).stream()
                .map(CompartidoResponse::from).toList();
    }

    private Evento buscarEvento(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }
}
