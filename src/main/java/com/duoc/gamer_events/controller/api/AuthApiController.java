package com.duoc.gamer_events.controller.api;

import com.duoc.gamer_events.dto.LoginRequest;
import com.duoc.gamer_events.dto.TokenResponse;
import com.duoc.gamer_events.dto.UsuarioResponse;
import com.duoc.gamer_events.model.RegistroForm;
import com.duoc.gamer_events.security.JwtService;
import com.duoc.gamer_events.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthApiController(AuthenticationManager authenticationManager, JwtService jwtService,
                             UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    // Valida las credenciales contra la BD y entrega un token JWT; si fallan responde 401
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        UserDetails usuario = (UserDetails) auth.getPrincipal();
        return new TokenResponse(jwtService.generarToken(usuario), "Bearer", jwtService.getExpiracionSegundos(),
                usuario.getUsername(), usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registro(@Valid @RequestBody RegistroForm form) {
        if (usuarioService.existe(form.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con este correo");
        }
        return UsuarioResponse.from(usuarioService.registrar(form));
    }
}
