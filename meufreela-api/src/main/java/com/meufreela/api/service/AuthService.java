package com.meufreela.api.service;

import com.meufreela.api.dto.AuthResponse;
import com.meufreela.api.dto.CadastroRequest;
import com.meufreela.api.dto.LoginRequest;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.exception.CpfJaCadastradoException;
import com.meufreela.api.exception.EmailJaCadastradoException;
import com.meufreela.api.repository.UsuarioRepository;
import com.meufreela.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse cadastrar(CadastroRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }
        if (repository.existsByCpf(request.cpf())) {
            throw new CpfJaCadastradoException(request.cpf()); // ← passa o CPF
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .cpf(request.cpf())
                .telefone(request.telefone())
                .tipo(request.tipo())
                .verificado(false)
                .build();

        repository.save(usuario);
        return gerarResposta(usuario);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        Usuario usuario = repository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return gerarResposta(usuario);
    }

    private AuthResponse gerarResposta(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", usuario.getId());
        claims.put("tipo", usuario.getTipo().name());
        claims.put("verificado", usuario.isVerificado());

        String token = jwtService.gerarToken(usuario, claims);
        String refresh = jwtService.gerarRefreshToken(usuario);

        return new AuthResponse(token, refresh, AuthResponse.UsuarioDto.from(usuario));
    }
}