package com.meufreela.api.security;

import tools.jackson.databind.ObjectMapper;
import com.meufreela.api.dto.EnviarMensagemRequest;
import com.meufreela.api.dto.MensagemResumo;
import com.meufreela.api.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final JwtService jwtService;
    private final ObjectMapper mapper; // ← injetado pelo Spring

    // solicitacaoId → sessões conectadas
    private final Map<String, Set<WebSocketSession>> salas = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = queryParam(session, "token");
        String solicitacaoId = queryParam(session, "solicitacao");

        if (token == null || solicitacaoId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        try {
            String email = jwtService.extrairUsername(token);
            session.getAttributes().put("email", email);
            session.getAttributes().put("solicitacaoId", solicitacaoId);

            salas.computeIfAbsent(solicitacaoId, k -> ConcurrentHashMap.newKeySet())
                    .add(session);
        } catch (Exception e) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String email = (String) session.getAttributes().get("email");
        String solicitacaoId = (String) session.getAttributes().get("solicitacaoId");

        if (email == null || solicitacaoId == null) return;

        try {
            EnviarMensagemRequest req = mapper.readValue(
                    message.getPayload(), EnviarMensagemRequest.class);

            MensagemResumo salva = chatService.salvar(email, solicitacaoId, req.conteudo());
            String json = mapper.writeValueAsString(salva);

            broadcast(solicitacaoId, new TextMessage(json));
        } catch (Exception e) {
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
            } catch (Exception ignored) {}
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