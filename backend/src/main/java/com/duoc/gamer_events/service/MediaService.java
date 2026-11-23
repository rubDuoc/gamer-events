package com.duoc.gamer_events.service;

import com.duoc.gamer_events.dto.ComunidadDtos.MediaResponse;
import com.duoc.gamer_events.model.Evento;
import com.duoc.gamer_events.model.Media;
import com.duoc.gamer_events.repository.EventoRepository;
import com.duoc.gamer_events.repository.MediaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Fotos y videos de los eventos. Reglas de seguridad para la subida de archivos:
// - solo el autor del evento o un ADMIN puede agregar o quitar contenido
// - fotos de maximo 2 MB, solo JPG, PNG o WEBP, comprobando la firma real del archivo (no la extension)
// - el nombre del archivo enviado nunca se usa; los videos solo se aceptan desde YouTube
@Service
@Transactional
public class MediaService {

    public static final long TAMANO_MAXIMO = 2L * 1024 * 1024;
    public static final int MAXIMO_POR_EVENTO = 10;

    // Acepta youtube.com/watch?v=ID, youtu.be/ID y youtube.com/shorts/ID; extrae el ID de 11 caracteres
    private static final Pattern YOUTUBE = Pattern.compile(
            "^(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/(?:watch\\?v=|shorts/)|youtu\\.be/)([A-Za-z0-9_-]{11})(?:[?&#][^\\s]*)?$");

    private final MediaRepository mediaRepository;
    private final EventoRepository eventoRepository;

    public MediaService(MediaRepository mediaRepository, EventoRepository eventoRepository) {
        this.mediaRepository = mediaRepository;
        this.eventoRepository = eventoRepository;
    }

    @Transactional(readOnly = true)
    public List<MediaResponse> listar(Long eventoId) {
        buscarEvento(eventoId);
        return mediaRepository.findByEventoIdOrderByFechaAsc(eventoId).stream().map(MediaResponse::from).toList();
    }

    public MediaResponse subirFoto(Long eventoId, MultipartFile archivo, String email, boolean esAdmin) {
        Evento evento = validarAutor(eventoId, email, esAdmin);
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen no puede superar 2 MB");
        }
        byte[] bytes = leer(archivo);
        String tipoReal = detectarTipo(bytes);
        if (tipoReal == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten imagenes JPG, PNG o WEBP");
        }
        Media media = nueva(evento, Media.Tipo.FOTO, email);
        media.setContenido(bytes);
        media.setContentType(tipoReal);
        return MediaResponse.from(mediaRepository.save(media));
    }

    public MediaResponse agregarVideo(Long eventoId, String url, String email, boolean esAdmin) {
        Evento evento = validarAutor(eventoId, email, esAdmin);
        Matcher matcher = YOUTUBE.matcher(url.trim());
        if (!matcher.matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se aceptan enlaces de videos de YouTube");
        }
        Media media = nueva(evento, Media.Tipo.VIDEO, email);
        media.setVideoId(matcher.group(1));
        return MediaResponse.from(mediaRepository.save(media));
    }

    public record Archivo(byte[] contenido, String contentType) {
    }

    // El contenido se lee dentro de la transaccion (la columna es LAZY)
    @Transactional(readOnly = true)
    public Archivo foto(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .filter(m -> m.getTipo() == Media.Tipo.FOTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto no encontrada"));
        return new Archivo(media.getContenido(), media.getContentType());
    }

    public void eliminar(Long mediaId, String email, boolean esAdmin) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contenido no encontrado"));
        validarAutor(media.getEvento().getId(), email, esAdmin);
        mediaRepository.delete(media);
    }

    // Solo el usuario que publico el evento o un ADMIN (los eventos iniciales no tienen autor: solo ADMIN)
    private Evento validarAutor(Long eventoId, String email, boolean esAdmin) {
        Evento evento = buscarEvento(eventoId);
        if (!esAdmin && !email.equals(evento.getAutorEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo quien publico el evento puede agregar o quitar fotos y videos");
        }
        return evento;
    }

    private Media nueva(Evento evento, Media.Tipo tipo, String email) {
        if (mediaRepository.countByEventoIdAndTipo(evento.getId(), tipo) >= MAXIMO_POR_EVENTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se alcanzo el maximo de " + MAXIMO_POR_EVENTO + " por evento");
        }
        Media media = new Media();
        media.setEvento(evento);
        media.setTipo(tipo);
        media.setAutorEmail(email);
        media.setFecha(LocalDateTime.now());
        return media;
    }

    // Firma ("magic bytes") de cada formato permitido: el contenido manda, no el nombre ni el Content-Type enviado
    static String detectarTipo(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "image/png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private static byte[] leer(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen");
        }
    }

    private Evento buscarEvento(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }
}
