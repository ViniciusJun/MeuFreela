package com.meufreela.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.FetchType;
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

    @Id
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

    // ============ Papeis de usuário ============
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "usuario_papeis",
            joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "papel")
    @Builder.Default
    private Set<TipoUsuario> papeis = new HashSet<>();

    public boolean temPapel(TipoUsuario papel) {
        return papeis.contains(papel);
    }

    public void adicionarPapel(TipoUsuario papel) {
        this.papeis.add(papel);
    }

    // ============ UserDetails ============

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return papeis.stream()
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