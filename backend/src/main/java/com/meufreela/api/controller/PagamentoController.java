package com.meufreela.api.controller;

import com.meufreela.api.dto.PagamentoResumo;
import com.meufreela.api.service.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solicitacoes/{id}/pagamento")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping
    public ResponseEntity<PagamentoResumo> pagar(
            Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(pagamentoService.processarPagamento(auth.getName(), id));
    }

    @PostMapping("/liberar")
    public ResponseEntity<PagamentoResumo> liberarEscrow(
            Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(pagamentoService.liberarEscrow(auth.getName(), id));
    }
}