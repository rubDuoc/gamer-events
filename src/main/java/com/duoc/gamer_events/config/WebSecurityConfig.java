package com.duoc.gamer_events.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(requests -> requests
                // Paginas publicas: inicio, registro, login y recursos estaticos
                .requestMatchers("/", "/home", "/registro", "/login", "/css/**", "/error").permitAll()
                // Todo lo demas (perfil, eventos) requiere autenticacion
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/eventos")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }

    // Usuarios en memoria (aun no hay base de datos); las claves se guardan cifradas con BCrypt
    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails jugador1 = User.builder()
                .username("jugador1@gamerevents.cl")
                .password(passwordEncoder.encode("Jugador1#2026"))
                .roles("USER")
                .build();
        UserDetails jugador2 = User.builder()
                .username("jugador2@gamerevents.cl")
                .password(passwordEncoder.encode("Jugador2#2026"))
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin@gamerevents.cl")
                .password(passwordEncoder.encode("Admin#2026"))
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(jugador1, jugador2, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
