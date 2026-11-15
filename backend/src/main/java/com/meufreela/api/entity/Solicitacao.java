package com.meufreela.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "freelancer_id")
    private Usuario freelancer;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false, length = 300)
    private String endereco;

    @Column(nullable = false)
    private LocalDateTime dataDesejada;

    @ManyToOne
    @JoinColumn(name = "servico_id")
    private Servico servico;

    private Integer duracaoHoras;

    private Double valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm;

    @OneToOne(mappedBy = "solicitacao", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Pagamento pagamento;
}