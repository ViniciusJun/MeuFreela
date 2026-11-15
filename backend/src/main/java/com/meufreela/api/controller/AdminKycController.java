package com.meufreela.api.controller;

import com.meufreela.api.entity.SolicitacaoKyc;
import com.meufreela.api.entity.StatusKyc;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.SolicitacaoKycRepository;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/kyc")
@RequiredArgsConstructor
public class AdminKycController {

    private final SolicitacaoKycRepository kycRepository;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<?> aprovar(@PathVariable String id) {
        SolicitacaoKyc kyc = kycRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        kyc.setStatus(StatusKyc.APROVADO);
        kyc.setDataAnalise(LocalDateTime.now());
        kycRepository.save(kyc);

        Usuario u = kyc.getUsuario();
        u.setVerificado(true);
        u.setStatusKyc(StatusKyc.APROVADO);
        usuarioRepository.save(u);

        return ResponseEntity.ok("Aprovado");
    }

    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<?> rejeitar(@PathVariable String id, @RequestParam String motivo) {
        SolicitacaoKyc kyc = kycRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        kyc.setStatus(StatusKyc.REJEITADO);
        kyc.setMotivoRejeicao(motivo);
        kyc.setDataAnalise(LocalDateTime.now());
        kycRepository.save(kyc);

        Usuario u = kyc.getUsuario();
        u.setStatusKyc(StatusKyc.REJEITADO);
        usuarioRepository.save(u);

        return ResponseEntity.ok("Rejeitado");
    }
}