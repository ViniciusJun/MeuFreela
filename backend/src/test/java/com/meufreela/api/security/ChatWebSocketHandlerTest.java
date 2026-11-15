package com.meufreela.api.security;

import com.meufreela.api.dto.EnviarMensagemRequest;
import com.meufreela.api.dto.MensagemResumo;
import com.meufreela.api.service.ChatService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes do ChatWebSocketHandler.
 *
 * Cobre especificamente as 2 vulnerabilidades fechadas:
 *
 *  1. Refresh token aceito como access no handshake (JWT type=refresh
 *     tem validade de 7 dias; sem checar, o WebSocket seria acessível
 *     por 7 dias com um token roubado).
 *
 *  2. IDOR no handshake: antes da correção, qualquer usuário autenticado
 *     podia escutar qualquer conversa; agora validarParticipacao() bloqueia
 *     quem não é cliente/freelancer da solicitação.
 *
 * Testar esses dois cenários é o que impede que alguém, no futuro,
 * "refatore" o handler e reintroduza as falhas.
 */
@ExtendWith(MockitoExtension.class)
class ChatWebSocketHandlerTest {

    @Mock private ChatService chatService;
    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private ObjectMapper mapper;
    @Mock private WebSocketSession session;

    @InjectMocks private ChatWebSocketHandler handler;

    private Map<String, Object> attributes;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        lenient().when(session.getAttributes()).thenReturn(attributes);
        lenient().when(session.getId()).thenReturn("session-1");

        userDetails = User.withUsername("joao@test.com")
                .password("x").authorities("ROLE_CLIENTE").build();
    }

    private void setUri(String query) {
        String base = "ws://localhost:8080/ws/chat";
        URI uri = query == null || query.isEmpty()
                ? URI.create(base)
                : URI.create(base + "?" + query);
        when(session.getUri()).thenReturn(uri);
    }

    // ==================== afterConnectionEstablished ====================

    @Test
    void handshake_semToken_deveFecharBadData() throws Exception {
        setUri("solicitacao=sol-1");   // tem solicitacao, falta token

        handler.afterConnectionEstablished(session);

        verify(session).close(eq(CloseStatus.BAD_DATA.withReason("token e solicitacao obrigatórios")));
    }

    @Test
    void handshake_semSolicitacao_deveFecharBadData() throws Exception {
        setUri("token=abc");           // tem token, falta solicitacao

        handler.afterConnectionEstablished(session);

        verify(session).close(eq(CloseStatus.BAD_DATA.withReason("token e solicitacao obrigatórios")));
    }

    @Test
    void handshake_tokenInvalido_deveFecharNotAcceptable() throws Exception {
        setUri("token=access-token&solicitacao=sol-1");

        when(jwtService.extrairUsername("access-token")).thenReturn("joao@test.com");
        when(userDetailsService.loadUserByUsername("joao@test.com")).thenReturn(userDetails);
        when(jwtService.tokenValido("access-token", userDetails)).thenReturn(false);

        handler.afterConnectionEstablished(session);

        verify(session).close(eq(CloseStatus.NOT_ACCEPTABLE.withReason("Token inválido")));
    }

    @Test
    void handshake_refreshTokenComoAccess_deveFecharNotAcceptable() throws Exception {
        // CENÁRIO CRÍTICO: alguém tenta usar refresh token (7 dias de validade)
        // no handshake do WebSocket. tokenValido() rejeita porque type != "access".
        setUri("token=refresh-token&solicitacao=sol-1");

        when(jwtService.extrairUsername("refresh-token")).thenReturn("joao@test.com");
        when(userDetailsService.loadUserByUsername("joao@test.com")).thenReturn(userDetails);
        // refresh token rejeitado por tokenValido (type=refresh, não access)
        when(jwtService.tokenValido("refresh-token", userDetails)).thenReturn(false);

        handler.afterConnectionEstablished(session);

        verify(session).close(eq(CloseStatus.NOT_ACCEPTABLE.withReason("Token inválido")));
    }

    @Test
    void handshake_naoParticipante_deveFecharPolicyViolation_comIdor() throws Exception {
        // CENÁRIO CRÍTICO (IDOR): usuário autenticado válido, mas não é
        // cliente nem freelancer desta solicitação. Antes da correção,
        // entrava na sala e recebia as mensagens dos outros.
        setUri("token=valid-token&solicitacao=sol-alheia");

        when(jwtService.extrairUsername("valid-token")).thenReturn("intruso@test.com");
        when(userDetailsService.loadUserByUsername("intruso@test.com")).thenReturn(userDetails);
        when(jwtService.tokenValido("valid-token", userDetails)).thenReturn(true);
        doThrow(new IllegalArgumentException("Você não participa desta conversa"))
                .when(chatService).validarParticipacao("intruso@test.com", "sol-alheia");

        handler.afterConnectionEstablished(session);

        verify(session).close(eq(CloseStatus.POLICY_VIOLATION
                .withReason("Você não participa desta conversa")));
    }

    @Test
    void handshake_usuarioDeletado_deveFecharNotAcceptable() throws Exception {
        setUri("token=valid-token&solicitacao=sol-1");

        when(jwtService.extrairUsername("valid-token")).thenReturn("fantasma@test.com");
        when(userDetailsService.loadUserByUsername("fantasma@test.com"))
                .thenThrow(new UsernameNotFoundException("Usuário não encontrado"));

        handler.afterConnectionEstablished(session);

        verify(session).close(eq(CloseStatus.NOT_ACCEPTABLE));
    }

    @Test
    void handshake_valido_deveAceitarConexaoEAdicionarNaSala() throws Exception {
        setUri("token=valid-token&solicitacao=sol-1");

        when(jwtService.extrairUsername("valid-token")).thenReturn("joao@test.com");
        when(userDetailsService.loadUserByUsername("joao@test.com")).thenReturn(userDetails);
        when(jwtService.tokenValido("valid-token", userDetails)).thenReturn(true);
        // validarParticipacao não lança → participante

        handler.afterConnectionEstablished(session);

        // Não deve fechar
        verify(session, never()).close(any());

        // Deve preencher atributos da sessão
        assertThat(attributes).containsEntry("email", "joao@test.com");
        assertThat(attributes).containsEntry("solicitacaoId", "sol-1");

        // Deve estar na sala "sol-1" (verificado via reflection no mapa privado)
        @SuppressWarnings("unchecked")
        Map<String, Set<WebSocketSession>> salas =
                (Map<String, Set<WebSocketSession>>) ReflectionTestUtils.getField(handler, "salas");
        assertThat(salas).containsKey("sol-1");
        assertThat(salas.get("sol-1")).contains(session);
    }

    // ==================== handleTextMessage ====================

    @Test
    void handleTextMessage_valido_deveSalvarMensagemEBroadcastar() throws Exception {
        // Setup: sessão já conectada (atributos preenchidos pelo handshake)
        attributes.put("email", "joao@test.com");
        attributes.put("solicitacaoId", "sol-1");

        // Simula a sessão estar na sala (após handshake válido)
        @SuppressWarnings("unchecked")
        Map<String, Set<WebSocketSession>> salas =
                (Map<String, Set<WebSocketSession>>) ReflectionTestUtils.getField(handler, "salas");
        salas.computeIfAbsent("sol-1", k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(session);
        when(session.isOpen()).thenReturn(true);

        when(mapper.readValue(anyString(), eq(EnviarMensagemRequest.class)))
                .thenReturn(new EnviarMensagemRequest("Olá, tudo bem?"));
        when(chatService.salvar("joao@test.com", "sol-1", "Olá, tudo bem?"))
                .thenReturn(new MensagemResumo(
                        "msg-1", "sol-1", "cli-1", "João Cliente",
                        "Olá, tudo bem?", null));
        when(mapper.writeValueAsString(any())).thenReturn("{\"id\":\"msg-1\"}");

        TextMessage incoming = new TextMessage("{\"conteudo\":\"Olá, tudo bem?\"}");
        handler.handleTextMessage(session, incoming);

        verify(chatService).salvar("joao@test.com", "sol-1", "Olá, tudo bem?");

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());
        assertThat(captor.getValue().getPayload()).contains("msg-1");
    }

    @Test
    void handleTextMessage_erroNoServico_deveEnviarMensagemDeErro() throws Exception {
        attributes.put("email", "joao@test.com");
        attributes.put("solicitacaoId", "sol-1");

        when(mapper.readValue(anyString(), eq(EnviarMensagemRequest.class)))
                .thenReturn(new EnviarMensagemRequest("Olá"));
        when(chatService.salvar(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Você não participa desta conversa"));

        TextMessage incoming = new TextMessage("{\"conteudo\":\"Olá\"}");
        handler.handleTextMessage(session, incoming);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());
        assertThat(captor.getValue().getPayload())
                .contains("erro")
                .contains("não participa");
    }
}