package com.meufreela.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacoes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"solicitacao_id", "autor_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitacao_id")
    private Solicitacao solicitacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "avaliado_id")
    private Usuario avaliado;

    @Column(nullable = false)
    private Integer nota; // 1 a 5

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();
}