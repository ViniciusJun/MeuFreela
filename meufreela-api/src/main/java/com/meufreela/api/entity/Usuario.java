package com.meufreela.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    // ============ Identificação ============

    // @Setter(AccessLevel.NONE) impede que o id seja sobrescrito após persistência
    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(length = 500)
    private String fcmToken;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false, length = 11)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo;

    // Freelancer com identidade validada (KYC, docs). NÃO confundir com "conta ativa".
    @Column(nullable = false)
    @Builder.Default
    private boolean verificado = false;

    // ============ Campos exclusivos de FREELANCER ============

    @Enumerated(EnumType.STRING)
    private CategoriaServico categoria;

    @Column(length = 500)
    private String descricao;

    private Double precoHora;

    @Builder.Default
    private Double avaliacaoMedia = 0.0;

    @Builder.Default
    private Integer totalServicos = 0;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime criadoEm = LocalDateTime.now();

    // ============ Enum de tipo de usuário ============

    public enum TipoUsuario {
        CLIENTE,
        FREELANCER
    }

    // ============ Papéis de usuário ============

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "usuario_papeis",
            joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "papel")
    @Builder.Default
    private Set<TipoUsuario> papeis = new HashSet<>();

    /**
     * Garante invariantes antes da primeira persistência:
     *  - `papeis` nunca fica null;
     *  - `papeis` nunca fica vazio quando `tipo` está preenchido,
     *    evitando usuário sem nenhuma autoridade.
     */
    @PrePersist
    private void garantirPapeis() {
        if (papeis == null) {
            papeis = new HashSet<>();
        }
        if (papeis.isEmpty() && tipo != null) {
            papeis.add(tipo);
        }
    }

    public boolean temPapel(TipoUsuario papel) {
        return papeis != null && papeis.contains(papel);
    }

    public void adicionarPapel(TipoUsuario papel) {
        if (this.papeis == null) {
            this.papeis = new HashSet<>();
        }
        this.papeis.add(papel);
    }

    // ============ UserDetails ============

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<TipoUsuario> papeisEfetivos = (papeis != null) ? papeis : Set.of();
        return papeisEfetivos.stream()
                .map(p -> new SimpleGrantedAuthority("ROLE_" + p.name()))
                .toList();
    }

    @Override
    public String getPassword() { return senha; }

    @Override
    public String getUsername() { return email; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}