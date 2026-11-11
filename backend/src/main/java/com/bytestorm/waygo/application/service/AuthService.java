package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.bytestorm.waygo.application.dto.request.LoginRequest;
import com.bytestorm.waygo.application.dto.request.RegistroRequest;
import com.bytestorm.waygo.application.dto.response.LoginResponse;
import com.bytestorm.waygo.application.dto.response.RegistroResponse;
import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.exception.CredenciaisInvalidasException;
import com.bytestorm.waygo.domain.exception.EmailJaCadastradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.SenhaHasher;
import com.bytestorm.waygo.domain.interfaces.TokenProvider;
import com.bytestorm.waygo.domain.interfaces.UsuarioRepository;
import com.bytestorm.waygo.domain.valueobject.TokenAcesso;
import com.bytestorm.waygo.infrastructure.config.security.UsuarioAutenticado;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final SenhaHasher senhaHasher;
    private final Clock clock;

    public AuthService(UsuarioRepository usuarioRepository, TokenProvider tokenProvider,
            AuthenticationManager authenticationManager, SenhaHasher senhaHasher, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.senhaHasher = senhaHasher;
        this.clock = clock;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = autenticar(request.email(), request.senha());
        TokenAcesso tokenAcesso = tokenProvider.gerarToken(usuario);
        return LoginResponse.de(tokenAcesso, clock.instant());
    }

    public RegistroResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existePorEmail(request.email())) {
            throw new EmailJaCadastradoException();
        }

        Usuario usuario = criarUsuario(request.email(), request.senha());

        try {
            return RegistroResponse.de(usuarioRepository.salvar(usuario));
        } catch (DataIntegrityViolationException e) {
            throw new EmailJaCadastradoException();
        }
    }

    private Usuario criarUsuario(String email, String senha) {
        try {
            return Usuario.criar(email, senha, senhaHasher, LocalDateTime.now(clock));
        } catch (IllegalArgumentException e) {
            throw new RequisicaoInvalidaException(e.getMessage());
        }
    }

    private Usuario autenticar(String email, String senha) {
        try {
            Authentication autenticacao = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, senha));
            return ((UsuarioAutenticado) autenticacao.getPrincipal()).getUsuario();
        } catch (AuthenticationException e) {
            throw new CredenciaisInvalidasException();
        }
    }

}
