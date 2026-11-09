package com.duoc.gamer_events.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Lee el header "Authorization: Bearer <token>" y, si el token es valido, autentica la peticion
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIJO)) {
            try {
                String email = jwtService.obtenerEmail(header.substring(PREFIJO.length()));
                // Se vuelve a cargar el usuario desde la BD para usar sus roles actuales
                UserDetails usuario = userDetailsService.loadUserByUsername(email);
                SecurityContext contexto = SecurityContextHolder.createEmptyContext();
                contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                        usuario, null, usuario.getAuthorities()));
                SecurityContextHolder.setContext(contexto);
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                // Token invalido o expirado: la peticion sigue sin autenticar y Spring Security responde 401
                log.debug("Token JWT rechazado: {}", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
