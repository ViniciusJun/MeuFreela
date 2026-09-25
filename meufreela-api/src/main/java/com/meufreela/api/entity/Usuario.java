package com.meufreela.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder  // ← obrigatórias
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha; // sempre armazenada com BCrypt

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false, length = 11)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo; // CLIENTE ou FREELANCER

    @Column(nullable = false)
    private boolean verificado = false; // KYC

    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum TipoUsuario {
        CLIENTE, FREELANCER
    }

    // ----- UserDetails -----

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + tipo.name()));
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