package com.meufreela.api.controller;

import com.meufreela.api.dto.SolicitacaoKycResumo;
import com.meufreela.api.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/usuarios/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> enviarDocumentos(
            Authentication auth,
            @RequestParam("documento") MultipartFile documento,
            @RequestParam("selfie") MultipartFile selfie) {
        kycService.enviarDocumentos(auth.getName(), documento, selfie);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<SolicitacaoKycResumo> status(Authentication auth) {
        return ResponseEntity.ok(kycService.statusAtual(auth.getName()));
    }
}