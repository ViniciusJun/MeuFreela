package com.meufreela.api.controller;

import com.meufreela.api.dto.CriarSolicitacaoRequest;
import com.meufreela.api.dto.SolicitacaoResumo;
import com.meufreela.api.entity.StatusSolicitacao;
import com.meufreela.api.service.SolicitacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController {

    private final SolicitacaoService service;

    /** Cliente cria uma nova solicitação. */
    @PostMapping
    public ResponseEntity<SolicitacaoResumo> criar(
            Authentication auth,
            @Valid @RequestBody CriarSolicitacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(auth.getName(), request));
    }

    /** Lista solicitações do cliente logado. */
    @GetMapping("/como-cliente")
    public ResponseEntity<List<SolicitacaoResumo>> comoCliente(Authentication auth) {
        return ResponseEntity.ok(service.minhasComoCliente(auth.getName()));
    }

    /** Lista solicitações do freelancer logado. */
    @GetMapping("/como-freelancer")
    public ResponseEntity<List<SolicitacaoResumo>> comoFreelancer(Authentication auth) {
        return ResponseEntity.ok(service.minhasComoFreelancer(auth.getName()));
    }

    /**
     * Atualiza o status de uma solicitação.
     * Ex.: PATCH /api/solicitacoes/{id}/status?status=ACEITA
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<SolicitacaoResumo> atualizarStatus(
            Authentication auth,
            @PathVariable String id,
            @RequestParam StatusSolicitacao status) {
        return ResponseEntity.ok(service.atualizarStatus(auth.getName(), id, status));
    }
}