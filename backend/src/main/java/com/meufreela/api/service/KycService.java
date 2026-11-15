package com.meufreela.api.service;

import com.meufreela.api.dto.SolicitacaoKycResumo;
import com.meufreela.api.entity.SolicitacaoKyc;
import com.meufreela.api.entity.StatusKyc;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.SolicitacaoKycRepository;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class KycService {

    private final UsuarioRepository usuarioRepository;
    private final SolicitacaoKycRepository kycRepository;
    private final UploadService uploadService;

    @Transactional
    public void enviarDocumentos(String email, MultipartFile documento, MultipartFile selfie) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (u.isVerificado()) {
            throw new IllegalArgumentException("Usuário já está verificado");
        }

        if (u.getStatusKyc() == StatusKyc.EM_ANALISE) {
            throw new IllegalArgumentException("Sua solicitação já está em análise");
        }

        try {
            String docUrl = uploadService.salvarDocumentoKyc(u.getId(), documento, "doc");
            String selfieUrl = uploadService.salvarDocumentoKyc(u.getId(), selfie, "selfie");

            SolicitacaoKyc kyc = SolicitacaoKyc.builder()
                    .usuario(u)
                    .documentoUrl(docUrl)
                    .selfieUrl(selfieUrl)
                    .status(StatusKyc.EM_ANALISE)
                    .build();

            kycRepository.save(kyc);

            u.setStatusKyc(StatusKyc.EM_ANALISE);
            usuarioRepository.save(u);

        } catch (IOException e) {
            throw new IllegalArgumentException("Erro ao salvar documentos: " + e.getMessage());
        }
    }

    public SolicitacaoKycResumo statusAtual(String email) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return kycRepository.findFirstByUsuarioOrderByDataEnvioDesc(u)
                .map(SolicitacaoKycResumo::from)
                .orElse(new SolicitacaoKycResumo(u.getStatusKyc(), null, null));
    }
}