package com.meufreela.api.controller;

import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository repository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        Usuario usuario = repository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(Map.of(
                "id", usuario.getId(),
                "nome", usuario.getNome(),
                "email", usuario.getEmail(),
                "tipo", usuario.getTipo().name(),
                "verificado", usuario.isVerificado()
        ));
    }
}