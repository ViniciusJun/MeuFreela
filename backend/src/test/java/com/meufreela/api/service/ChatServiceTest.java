package com.meufreela.api.service;

import com.meufreela.api.dto.MensagemResumo;
import com.meufreela.api.entity.Mensagem;
import com.meufreela.api.entity.Solicitacao;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.MensagemRepository;
import com.meufreela.api.repository.SolicitacaoRepository;
import com.meufreela.api.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do ChatService.
 *
 * Cobre as regras de negócio:
 *  - Envio de mensagens com notificação ao destinatário correto
 *  - Validação de participação (cliente OU freelancer da solicitação)
 *  - Histórico acessível apenas por participantes
 *
 * A correção IDOR do WebSocket depende de validarParticipacao() funcionar
 * corretamente — estes testes são a linha de defesa contra regressão.
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private MensagemRepository mensagemRepository;
    @Mock private SolicitacaoRepository solicitacaoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private NotificacaoService notificacaoService;

    @InjectMocks private ChatService chatService;

    private Usuario cliente;
    private Usuario freelancer;
    private Solicitacao solicitacao;

    @BeforeEach
    void setUp() {
        cliente = Usuario.builder()
                .nome("João Cliente")
                .email("joao@test.com")
                .tipo(Usuario.TipoUsuario.CLIENTE)
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.CLIENTE)))
                .build();
        ReflectionTestUtils.setField(cliente, "id", "cli-1");

        freelancer = Usuario.builder()
                .nome("Carlos Eletricista")
                .email("carlos@test.com")
                .tipo(Usuario.TipoUsuario.FREELANCER)
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.FREELANCER)))
                .build();
        ReflectionTestUtils.setField(freelancer, "id", "fre-1");

        solicitacao = Solicitacao.builder()
                .cliente(cliente)
                .freelancer(freelancer)
                .build();
        ReflectionTestUtils.setField(solicitacao, "id", "sol-1");
    }

    // ==================== salvar ====================

    @Test
    void salvar_clienteEnvia_deveNotificarFreelancer() {
        when(usuarioRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(cliente));
        when(solicitacaoRepository.findById("sol-1")).thenReturn(Optional.of(solicitacao));
        when(mensagemRepository.save(any(Mensagem.class)))
                .thenAnswer(inv -> {
                    Mensagem m = inv.getArgument(0);
                    ReflectionTestUtils.setField(m, "id", "msg-1");
                    return m;
                });

        MensagemResumo resp = chatService.salvar("joao@test.com", "sol-1", "Olá, tudo bem?");

        assertThat(resp.conteudo()).isEqualTo("Olá, tudo bem?");
        assertThat(resp.remetenteId()).isEqualTo("cli-1");
        assertThat(resp.remetenteNome()).isEqualTo("João Cliente");
        assertThat(resp.solicitacaoId()).isEqualTo("sol-1");

        // Cliente envia → notificação vai para o freelancer
        verify(notificacaoService).enviar(
                eq(freelancer), anyString(), anyString(), anyMap());
    }

    @Test
    void salvar_freelancerEnvia_deveNotificarCliente() {
        when(usuarioRepository.findByEmail("carlos@test.com")).thenReturn(Optional.of(freelancer));
        when(solicitacaoRepository.findById("sol-1")).thenReturn(Optional.of(solicitacao));
        when(mensagemRepository.save(any(Mensagem.class)))
                .thenAnswer(inv -> {
                    Mensagem m = inv.getArgument(0);
                    ReflectionTestUtils.setField(m, "id", "msg-2");
                    return m;
                });

        MensagemResumo resp = chatService.salvar("carlos@test.com", "sol-1", "Posso amanhã?");

        assertThat(resp.remetenteId()).isEqualTo("fre-1");

        // Freelancer envia → notificação vai para o cliente
        verify(notificacaoService).enviar(
                eq(cliente), anyString(), anyString(), anyMap());
    }

    @Test
    void salvar_naoParticipante_deveLancarExcecao() {
        Usuario intruso = Usuario.builder()
                .nome("Intruso")
                .email("intruso@test.com")
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.CLIENTE)))
                .tipo(Usuario.TipoUsuario.CLIENTE)
                .build();
        ReflectionTestUtils.setField(intruso, "id", "intruso-1");

        when(usuarioRepository.findByEmail("intruso@test.com")).thenReturn(Optional.of(intruso));
        when(solicitacaoRepository.findById("sol-1")).thenReturn(Optional.of(solicitacao));

        assertThatThrownBy(() ->
                chatService.salvar("intruso@test.com", "sol-1", "Quero entrar"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não participa");

        verify(mensagemRepository, never()).save(any());
        verify(notificacaoService, never()).enviar(any(), any(), any(), any());
    }

    @Test
    void salvar_solicitacaoInexistente_deveLancarExcecao() {
        when(usuarioRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(cliente));
        when(solicitacaoRepository.findById("sol-x")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                chatService.salvar("joao@test.com", "sol-x", "Olá"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solicitação não encontrada");
    }

    // ==================== historico ====================

    @Test
    void historico_participante_deveRetornarLista() {
        Mensagem m1 = Mensagem.builder()
                .solicitacao(solicitacao)
                .remetente(cliente)
                .conteudo("Primeira")
                .build();
        ReflectionTestUtils.setField(m1, "id", "msg-1");

        Mensagem m2 = Mensagem.builder()
                .solicitacao(solicitacao)
                .remetente(freelancer)
                .conteudo("Segunda")
                .build();
        ReflectionTestUtils.setField(m2, "id", "msg-2");

        when(usuarioRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(cliente));
        when(solicitacaoRepository.findById("sol-1")).thenReturn(Optional.of(solicitacao));
        when(mensagemRepository.findBySolicitacaoOrderByEnviadaEmAsc(solicitacao))
                .thenReturn(List.of(m1, m2));

        List<MensagemResumo> resp = chatService.historico("joao@test.com", "sol-1");

        assertThat(resp).hasSize(2);
        assertThat(resp.get(0).conteudo()).isEqualTo("Primeira");
        assertThat(resp.get(1).conteudo()).isEqualTo("Segunda");
    }

    @Test
    void historico_naoParticipante_deveLancarExcecao() {
        Usuario intruso = Usuario.builder()
                .nome("Intruso")
                .email("intruso@test.com")
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.CLIENTE)))
                .tipo(Usuario.TipoUsuario.CLIENTE)
                .build();
        ReflectionTestUtils.setField(intruso, "id", "intruso-1");

        when(usuarioRepository.findByEmail("intruso@test.com")).thenReturn(Optional.of(intruso));
        when(solicitacaoRepository.findById("sol-1")).thenReturn(Optional.of(solicitacao));

        assertThatThrownBy(() ->
                chatService.historico("intruso@test.com", "sol-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não participa");

        verify(mensagemRepository, never()).findBySolicitacaoOrderByEnviadaEmAsc(any());
    }

    // ==================== validarParticipacao (usado pelo WebSocket handshake) ====================

    @Test
    void validarParticipacao_participante_deveRetornarSolicitacao() {
        when(usuarioRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(cliente));
        when(solicitacaoRepository.findById("sol-1")).thenReturn(Optional.of(solicitacao));

        Solicitacao s = chatService.validarParticipacao("joao@test.com", "sol-1");

        assertThat(s).isEqualTo(solicitacao);
    }

    @Test
    void validarParticipacao_naoParticipante_deveLancarExcecao() {
        Usuario intruso = Usuario.builder()
                .nome("Intruso")
                .email("intruso@test.com")
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.CLIENTE)))
                .tipo(Usuario.TipoUsuario.CLIENTE)
                .build();
        ReflectionTestUtils.setField(intruso, "id", "intruso-1");

        when(usuarioRepository.findByEmail("intruso@test.com")).thenReturn(Optional.of(intruso));
        when(solicitacaoRepository.findById("sol-1")).thenReturn(Optional.of(solicitacao));

        // Este é o teste que protege contra regressão do IDOR no WebSocket:
        // se alguém remover a checagem de participação, este teste quebra.
        assertThatThrownBy(() ->
                chatService.validarParticipacao("intruso@test.com", "sol-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não participa");
    }
}