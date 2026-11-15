package com.meufreela.api.service;

import com.meufreela.api.dto.AuthResponse;
import com.meufreela.api.dto.CadastroRequest;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.exception.CpfJaCadastradoException;
import com.meufreela.api.exception.CredenciaisInvalidasException;
import com.meufreela.api.exception.EmailJaCadastradoException;
import com.meufreela.api.repository.UsuarioRepository;
import com.meufreela.api.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .nome("João Test")
                .email("joao@test.com")
                .senha("hashed")
                .cpf("12345678901")
                .telefone("11999999999")
                .tipo(Usuario.TipoUsuario.CLIENTE)
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.CLIENTE)))
                .build();
        ReflectionTestUtils.setField(usuario, "id", "user-1");
    }

    private CadastroRequest requestExemplo() {
        return new CadastroRequest(
                "João Test", "joao@test.com", "senha12345",
                "12345678901", "11999999999", Usuario.TipoUsuario.CLIENTE);
    }

    // ==================== cadastrar ====================

    @Test
    void cadastrar_comSucesso_deveRetornarTokens() {
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByCpf(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", "mock-id-1");
            return u;
        });
        when(jwtService.gerarToken(any(Usuario.class), anyMap())).thenReturn("token-abc");
        when(jwtService.gerarRefreshToken(any(Usuario.class))).thenReturn("refresh-xyz");

        AuthResponse resp = authService.cadastrar(requestExemplo());

        assertThat(resp.token()).isEqualTo("token-abc");
        assertThat(resp.refreshToken()).isEqualTo("refresh-xyz");
        assertThat(resp.usuario().email()).isEqualTo("joao@test.com");
        assertThat(resp.usuario().tipo()).isEqualTo("CLIENTE");
        verify(repository).save(any(Usuario.class));
    }

    @Test
    void cadastrar_comEmailDuplicado_deveLancarExcecao() {
        when(repository.existsByEmail("joao@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.cadastrar(requestExemplo()))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void cadastrar_comCpfDuplicado_deveLancarExcecao() {
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> authService.cadastrar(requestExemplo()))
                .isInstanceOf(CpfJaCadastradoException.class);

        verify(repository, never()).save(any());
    }

    // ==================== refresh ====================

    @Test
    void refresh_comRefreshValido_deveRetornarNovosTokens() {
        String refreshToken = "refresh-valido";
        when(jwtService.extrairUsername(refreshToken)).thenReturn("joao@test.com");
        when(repository.findByEmail("joao@test.com")).thenReturn(Optional.of(usuario));
        when(jwtService.refreshTokenValido(refreshToken, usuario)).thenReturn(true);
        when(jwtService.gerarToken(any(Usuario.class), anyMap())).thenReturn("novo-access");
        when(jwtService.gerarRefreshToken(usuario)).thenReturn("novo-refresh");

        AuthResponse resp = authService.refresh(refreshToken);

        assertThat(resp.token()).isEqualTo("novo-access");
        assertThat(resp.refreshToken()).isEqualTo("novo-refresh");
        assertThat(resp.usuario().email()).isEqualTo("joao@test.com");
    }

    @Test
    void refresh_comTokenMalformado_deveLancarExcecao() {
        when(jwtService.extrairUsername("lixo"))
                .thenThrow(new RuntimeException("JWT malformado"));

        assertThatThrownBy(() -> authService.refresh("lixo"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void refresh_comAccessTokenUsadoComoRefresh_deveLancarExcecao() {
        // Cenário de segurança: alguém tenta usar access token no endpoint de refresh
        String accessToken = "access-disfarcado-de-refresh";
        when(jwtService.extrairUsername(accessToken)).thenReturn("joao@test.com");
        when(repository.findByEmail("joao@test.com")).thenReturn(Optional.of(usuario));
        // refreshTokenValido retorna false porque type != "refresh"
        when(jwtService.refreshTokenValido(accessToken, usuario)).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(accessToken))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    void refresh_comUsuarioDeletado_deveLancarExcecao() {
        when(jwtService.extrairUsername("refresh-de-usuario-fantasma"))
                .thenReturn("fantasma@test.com");
        when(repository.findByEmail("fantasma@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("refresh-de-usuario-fantasma"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void refresh_comRefreshExpirado_deveLancarExcecao() {
        String refreshToken = "refresh-expirado";
        when(jwtService.extrairUsername(refreshToken)).thenReturn("joao@test.com");
        when(repository.findByEmail("joao@test.com")).thenReturn(Optional.of(usuario));
        when(jwtService.refreshTokenValido(refreshToken, usuario)).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}