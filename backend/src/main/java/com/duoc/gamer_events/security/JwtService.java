package com.duoc.gamer_events.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration expiracion;

    // La clave se lee desde la variable de entorno JWT_SECRET (minimo 32 caracteres, HS256).
    // Si no se define, se genera una clave aleatoria y los tokens dejan de servir al reiniciar.
    public JwtService(@Value("${jwt.secret:}") String secreto,
                      @Value("${jwt.expiracion-minutos:60}") long minutos) {
        if (secreto.isBlank()) {
            log.warn("JWT_SECRET no definida: se usara una clave aleatoria temporal");
            this.clave = Jwts.SIG.HS256.key().build();
        } else {
            this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        }
        this.expiracion = Duration.ofMinutes(minutos);
    }

    public String generarToken(UserDetails usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("roles", usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracion)))
                .signWith(clave)
                .compact();
    }

    // Verifica la firma y la expiracion; lanza JwtException si el token no es valido
    public String obtenerEmail(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public long getExpiracionSegundos() {
        return expiracion.toSeconds();
    }
}
