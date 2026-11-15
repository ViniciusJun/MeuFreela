package com.meufreela.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_kyc")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitacaoKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private String documentoUrl; // Salvo em private_uploads

    @Column(nullable = false)
    private String selfieUrl;    // Salvo em private_uploads

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusKyc status;

    @Column(length = 500)
    private String motivoRejeicao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEnvio;

    private LocalDateTime dataAnalise;

    @PrePersist
    protected void onCreate() {
        dataEnvio = LocalDateTime.now();
        if (status == null) status = StatusKyc.EM_ANALISE;
    }
}