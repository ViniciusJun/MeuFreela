package com.meufreela.api.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import java.util.ArrayList;
import java.util.List;

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
    @Builder.Default                                      // ← ADICIONE
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;

    @Column(nullable = false, updatable = false)
    @Builder.Default                                      // ← ADICIONE
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "servico_fotos",
            joinColumns = @JoinColumn(name = "servico_id"))
    @Column(name = "foto_url", length = 500)
    @Builder.Default
    private List<String> fotos = new ArrayList<>();
}