package com.meufreela.api.security;

import com.meufreela.api.dto.EnviarMensagemRequest;
import com.meufreela.api.dto.MensagemResumo;
import com.meufreela.api.service.ChatService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ChatService chatService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper mapper;

    private final Map<String, Set<WebSocketSession>> salas = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = queryParam(session, "token");
        String solicitacaoId = queryParam(session, "solicitacao");

        if (token == null || solicitacaoId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("token e solicitacao obrigatórios"));
            return;
        }

        try {
            String email = jwtService.extrairUsername(token);

            // CORREÇÃO #1: valida type=access (refresh token de 7 dias não abre WebSocket)
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.tokenValido(token, userDetails)) {
                log.info("WebSocket rejeitado: token inválido/expirado para {}", email);
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Token inválido"));
                return;
            }

            // CORREÇÃO #2: valida participação ANTES de entrar na sala (fecha IDOR)
            // Antes, qualquer usuário autenticado podia escutar qualquer solicitação.
            chatService.validarParticipacao(email, solicitacaoId);

            session.getAttributes().put("email", email);
            session.getAttributes().put("solicitacaoId", solicitacaoId);

            salas.computeIfAbsent(solicitacaoId, k -> ConcurrentHashMap.newKeySet())
                    .add(session);

            log.info("WebSocket conectado: {} → solicitação {}", email, solicitacaoId);

        } catch (IllegalArgumentException e) {
            // Pode ser: usuário não encontrado, solicitação não encontrada,
            // ou usuário não participa da solicitação.
            log.info("WebSocket rejeitado ({}): {}", session.getId(), e.getMessage());
            session.close(CloseStatus.POLICY_VIOLATION.withReason(e.getMessage()));

        } catch (Exception e) {
            // Erros de JWT (exp, assinatura, malformado) caem aqui.
            log.info("WebSocket rejeitado ({}): token inválido — {}",
                    session.getId(), e.getMessage());
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String email = (String) session.getAttributes().get("email");
        String solicitacaoId = (String) session.getAttributes().get("solicitacaoId");

        if (email == null || solicitacaoId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        try {
            EnviarMensagemRequest req = mapper.readValue(
                    message.getPayload(), EnviarMensagemRequest.class);

            // salvar() já valida participação internamente
            MensagemResumo salva = chatService.salvar(email, solicitacaoId, req.conteudo());
            String json = mapper.writeValueAsString(salva);

            broadcast(solicitacaoId, new TextMessage(json));
        } catch (Exception e) {
            log.warn("Erro ao processar mensagem de {} em {}: {}",
                    email, solicitacaoId, e.getMessage());
            session.sendMessage(new TextMessage(
                    "{\"erro\":\"" + e.getMessage().replace("\"", "'") + "\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String solicitacaoId = (String) session.getAttributes().get("solicitacaoId");
        if (solicitacaoId == null) return;
        Set<WebSocketSession> sessoes = salas.get(solicitacaoId);
        if (sessoes != null) {
            sessoes.remove(session);
            if (sessoes.isEmpty()) salas.remove(solicitacaoId);
        }
    }

    private void broadcast(String sala, TextMessage msg) {
        Set<WebSocketSession> sessoes = salas.getOrDefault(sala, Set.of());
        for (WebSocketSession s : sessoes) {
            try {
                if (s.isOpen()) s.sendMessage(msg);
            } catch (Exception e) {
                // CORREÇÃO #3: não engole silenciosamente — loga para diagnóstico
                log.warn("Falha ao enviar mensagem para sessão {}: {}",
                        s.getId(), e.getMessage());
            }
        }
    }

    private String queryParam(WebSocketSession session, String chave) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) return null;
        for (String par : uri.getQuery().split("&")) {
            String[] kv = par.split("=");
            if (kv.length == 2 && kv[0].equals(chave)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}