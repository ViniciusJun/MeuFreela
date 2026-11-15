package com.meufreela.api.controller;

import com.meufreela.api.dto.AvaliacaoResumo;
import com.meufreela.api.dto.CriarAvaliacaoRequest;
import com.meufreela.api.service.AvaliacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService service;

    @PostMapping
    public ResponseEntity<AvaliacaoResumo> criar(
            Authentication auth,
            @Valid @RequestBody CriarAvaliacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(auth.getName(), request));
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<AvaliacaoResumo>> doUsuario(@PathVariable String id) {
        return ResponseEntity.ok(service.doUsuario(id));
    }
}