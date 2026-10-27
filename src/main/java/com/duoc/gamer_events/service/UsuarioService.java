package com.duoc.gamer_events.service;

import com.duoc.gamer_events.model.RegistroForm;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UsuarioService {

    private final InMemoryUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> nombres = new ConcurrentHashMap<>();

    public UsuarioService(InMemoryUserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existe(String email) {
        return userDetailsManager.userExists(normalizar(email));
    }

    // Registra al usuario con rol USER; la contraseña nunca se guarda en texto plano
    public void registrar(RegistroForm form) {
        String email = normalizar(form.getEmail());
        userDetailsManager.createUser(User.builder()
                .username(email)
                .password(passwordEncoder.encode(form.getPassword()))
                .roles("USER")
                .build());
        nombres.put(email, form.getNombre().trim());
    }

    public String obtenerNombre(String email) {
        return nombres.getOrDefault(email, email);
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
