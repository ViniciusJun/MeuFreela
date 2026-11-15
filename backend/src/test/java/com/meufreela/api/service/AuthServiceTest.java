package com.meufreela.api.service;

import com.meufreela.api.dto.AuthResponse;
import com.meufreela.api.dto.CadastroRequest;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.exception.CpfJaCadastradoException;
import com.meufreela.api.exception.EmailJaCadastradoException;
import com.meufreela.api.repository.UsuarioRepository;
import com.meufreela.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

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

    private CadastroRequest requestExemplo() {
        return new CadastroRequest(
                "João Test", "joao@test.com", "senha12345",
                "12345678901", "11999999999", Usuario.TipoUsuario.CLIENTE);
    }

    @Test
    void cadastrar_comSucesso_deveRetornarTokens() {
        // Arrange
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByCpf(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            // ReflectionTestUtils: simula o id gerado pelo JPA após o save(),
            // sem expor um setter público em produção.
            ReflectionTestUtils.setField(u, "id", "mock-id-1");
            return u;
        });
        when(jwtService.gerarToken(any(Usuario.class), anyMap())).thenReturn("token-abc");
        when(jwtService.gerarRefreshToken(any(Usuario.class))).thenReturn("refresh-xyz");

        // Act
        AuthResponse resp = authService.cadastrar(requestExemplo());

        // Assert
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
}