package com.meufreela.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "solicitacao_id", nullable = false, unique = true)
    private Solicitacao solicitacao;

    @Column(nullable = false)
    private Double valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusPagamento status = StatusPagamento.PENDENTE;

    // Simula o ID da transação no gateway (Stripe/MercadoPago)
    @Column(length = 100)
    private String gatewayTransactionId;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime liberadoEm;
}
