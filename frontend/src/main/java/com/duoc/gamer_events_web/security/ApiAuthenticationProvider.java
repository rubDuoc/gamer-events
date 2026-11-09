package com.duoc.gamer_events_web.security;

import com.duoc.gamer_events_web.client.BackendClient;
import com.duoc.gamer_events_web.client.Respuestas;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

// Valida el login del formulario contra la API del backend (/api/auth/login) y guarda el JWT recibido
@Component
public class ApiAuthenticationProvider implements AuthenticationProvider {

    private final BackendClient backendClient;

    public ApiAuthenticationProvider(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String email = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        try {
            Respuestas.Token respuesta = backendClient.login(email, password);
            UsuarioSesion usuario = new UsuarioSesion(respuesta.email(), respuesta.nombre(), respuesta.token());
            return UsernamePasswordAuthenticationToken.authenticated(usuario, null,
                    respuesta.roles().stream().map(SimpleGrantedAuthority::new).toList());
        } catch (HttpClientErrorException e) {
            // 400 o 401 del backend: mismo mensaje, sin revelar si el correo existe
            throw new BadCredentialsException("Correo o contraseña incorrectos");
        } catch (RestClientException e) {
            throw new AuthenticationServiceException("El servicio de autenticacion no esta disponible");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
