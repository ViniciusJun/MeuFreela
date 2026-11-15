package com.meufreela.api.controller;

import com.meufreela.api.dto.CriarServicoRequest;
import com.meufreela.api.dto.ServicoResumo;
import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.service.ServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService service;

    @GetMapping
    public ResponseEntity<List<ServicoResumo>> listar(
            @RequestParam(required = false) CategoriaServico categoria) {
        return ResponseEntity.ok(service.listar(categoria));
    }

    @GetMapping("/meus")
    public ResponseEntity<List<ServicoResumo>> meus(Authentication auth) {
        return ResponseEntity.ok(service.meus(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResumo> detalhe(@PathVariable String id) {
        return ResponseEntity.ok(service.detalhe(id));
    }

    @PostMapping
    public ResponseEntity<ServicoResumo> criar(
            Authentication auth,
            @Valid @RequestBody CriarServicoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(auth.getName(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResumo> atualizar(
            Authentication auth,
            @PathVariable String id,
            @Valid @RequestBody CriarServicoRequest req) {
        return ResponseEntity.ok(service.atualizar(auth.getName(), id, req));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<ServicoResumo> alternarAtivo(
            Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(service.alternarAtivo(auth.getName(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(Authentication auth, @PathVariable String id) {
        service.excluir(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/fotos", consumes = "multipart/form-data")
    public ResponseEntity<ServicoResumo> uploadFoto(
            Authentication auth,
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.adicionarFoto(auth.getName(), id, file));
    }

    @DeleteMapping("/{id}/fotos")
    public ResponseEntity<ServicoResumo> removerFoto(
            Authentication auth,
            @PathVariable String id,
            @RequestParam("url") String url) {
        return ResponseEntity.ok(service.removerFoto(auth.getName(), id, url));
    }
}
