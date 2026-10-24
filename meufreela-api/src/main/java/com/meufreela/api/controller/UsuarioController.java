package com.meufreela.api.controller;

import com.meufreela.api.dto.TornarFreelancerRequest;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.meufreela.api.dto.SalvarFcmTokenRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository repository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        Usuario u = repository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "nome", u.getNome(),
                "email", u.getEmail(),
                "tipo", u.getTipo().name(),
                "papeis", u.getPapeis().stream().map(Enum::name).toList(),
                "verificado", u.isVerificado()
        ));
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<?> salvarFcmToken(
            Authentication auth,
            @Valid @RequestBody SalvarFcmTokenRequest req) {   // ← a classe precisa existir
        Usuario u = repository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        u.setFcmToken(req.token());
        repository.save(u);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/tornar-freelancer")
    public ResponseEntity<Map<String, Object>> tornarFreelancer(
            Authentication auth,
            @Valid @RequestBody TornarFreelancerRequest req) {

        Usuario u = repository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (u.temPapel(Usuario.TipoUsuario.FREELANCER)) {
            throw new IllegalArgumentException("Você já é freelancer");
        }

        u.setCategoria(req.categoria());
        u.setDescricao(req.descricao());
        u.setPrecoHora(req.precoHora());
        u.adicionarPapel(Usuario.TipoUsuario.FREELANCER);
        repository.save(u);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Perfil de freelancer ativado!",
                "papeis", u.getPapeis().stream().map(Enum::name).toList()
        ));
    }
}