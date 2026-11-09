package com.duoc.gamer_events_web.config;

import com.duoc.gamer_events_web.security.ApiAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    // Paginas web (Thymeleaf): login con formulario y sesion; las credenciales se validan en el backend
    @Bean
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http,
                                                      ApiAuthenticationProvider apiAuthenticationProvider) throws Exception {
        http
            .authenticationProvider(apiAuthenticationProvider)
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
}
