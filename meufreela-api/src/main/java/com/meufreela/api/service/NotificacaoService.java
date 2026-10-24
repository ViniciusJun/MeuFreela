package com.meufreela.api.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.meufreela.api.entity.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class NotificacaoService {

    @Async
    public void enviar(Usuario destinatario, String titulo, String corpo,
                       Map<String, String> data) {
        String token = destinatario != null ? destinatario.getFcmToken() : null;
        if (token == null || token.isEmpty()) return;

        try {
            Message msg = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(corpo)
                            .build())
                    .putAllData(data != null ? data : Map.of())
                    .build();

            FirebaseMessaging.getInstance().send(msg);
        } catch (FirebaseMessagingException e) {
            log.warn("Falha ao enviar push para {}: {}",
                    destinatario.getEmail(), e.getMessage());
        }
    }
}