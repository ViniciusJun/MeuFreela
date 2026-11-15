package com.meufreela.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "servicos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "freelancer_id")
    private Usuario freelancer;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaServico categoria;

    @Column(nullable = false)
    private Double preco;

    @Builder.Default
    private boolean ativo = true;

    // NOVO: Lista de URLs das fotos do serviço
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "servico_fotos", joinColumns = @JoinColumn(name = "servico_id"))
    @Column(name = "foto_url", length = 500)
    @Builder.Default
    private List<String> fotos = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}