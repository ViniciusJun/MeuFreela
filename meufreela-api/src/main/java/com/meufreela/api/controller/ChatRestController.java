package com.meufreela.api.controller;

import com.meufreela.api.dto.EnviarMensagemRequest;
import com.meufreela.api.dto.MensagemResumo;
import com.meufreela.api.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes/{id}/mensagens")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService service;

    @GetMapping
    public List<MensagemResumo> historico(Authentication auth, @PathVariable String id) {
        return service.historico(auth.getName(), id);
    }

    @PostMapping
    public MensagemResumo enviar(Authentication auth, @PathVariable String id,
                                 @Valid @RequestBody EnviarMensagemRequest req) {
        return service.salvar(auth.getName(), id, req.conteudo());
    }
}