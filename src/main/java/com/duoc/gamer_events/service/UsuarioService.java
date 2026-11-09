package com.duoc.gamer_events.service;

import com.duoc.gamer_events.model.RegistroForm;
import com.duoc.gamer_events.model.Rol;
import com.duoc.gamer_events.model.Usuario;
import com.duoc.gamer_events.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existe(String email) {
        return usuarioRepository.existsByEmail(normalizar(email));
    }

    // Registra al usuario con rol USER; la contraseña nunca se guarda en texto plano
    public Usuario registrar(RegistroForm form) {
        return usuarioRepository.save(nuevoUsuario(form.getNombre().trim(), normalizar(form.getEmail()),
                form.getPassword(), Rol.USER));
    }

    public Usuario nuevoUsuario(String nombre, String email, String password, Rol rol) {
        Usuario usuario = new Usuario(nombre, email, passwordEncoder.encode(password), rol);
        usuario.setAvatar(PerfilService.AVATARES.get(0));
        usuario.setNotificarPorEmail(true);
        return usuario;
    }

    public String obtenerNombre(String email) {
        return usuarioRepository.findByEmail(email).map(Usuario::getNombre).orElse(email);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
