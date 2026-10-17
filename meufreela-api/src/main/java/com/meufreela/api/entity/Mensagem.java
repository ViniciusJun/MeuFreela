package com.meufreela.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitacao_id")
    private Solicitacao solicitacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "remetente_id")
    private Usuario remetente;

    @Column(nullable = false, length = 2000)
    private String conteudo;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime enviadaEm = LocalDateTime.now();

    @Builder.Default
    private boolean lida = false;
}