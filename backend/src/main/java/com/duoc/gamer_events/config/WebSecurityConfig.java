package com.duoc.gamer_events.config;

import com.duoc.gamer_events.security.JwtAuthenticationFilter;
import com.duoc.gamer_events.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    // API REST: sin sesion, se autentica con un token JWT en cada peticion
    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, JwtService jwtService,
                                                      UserDetailsService userDetailsService) throws Exception {
        http
            // El token viaja en el header Authorization y no en una cookie, por eso no aplica CSRF
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests
                // APIs publicas: login, registro e inicio
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/inicio").permitAll()
                // Solo administradores
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Todo lo demas (eventos, perfil) requiere token
                .anyRequest().authenticated()
            )
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, e) ->
                        responderError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token ausente, invalido o expirado"))
                .accessDeniedHandler((request, response, e) ->
                        responderError(response, HttpServletResponse.SC_FORBIDDEN, "No tienes permisos para este recurso"))
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Valida email y contraseña contra los usuarios de la BD (lo usa la API de login)
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void responderError(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + mensaje + "\"}");
    }
}
