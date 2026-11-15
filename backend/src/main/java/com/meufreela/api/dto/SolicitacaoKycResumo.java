package com.meufreela.api.dto;

import com.meufreela.api.entity.SolicitacaoKyc;
import com.meufreela.api.entity.StatusKyc;

import java.time.LocalDateTime;

public record SolicitacaoKycResumo(
        StatusKyc status,
        LocalDateTime dataEnvio,
        String motivoRejeicao
) {
    public static SolicitacaoKycResumo from(SolicitacaoKyc kyc) {
        return new SolicitacaoKycResumo(kyc.getStatus(), kyc.getDataEnvio(), kyc.getMotivoRejeicao());
    }
}