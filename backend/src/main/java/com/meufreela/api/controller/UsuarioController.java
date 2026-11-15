package com.meufreela.api.controller;

import com.meufreela.api.dto.SalvarFcmTokenRequest;
import com.meufreela.api.dto.TornarFreelancerRequest;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.UsuarioRepository;
import com.meufreela.api.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository repository;
    private final UploadService uploadService;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        Usuario u = repository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // HashMap com put() é mais legível que Map.of() com 7 pares
        Map<String, Object> response = new HashMap<>();
        response.put("id", u.getId());
        response.put("nome", u.getNome());
        response.put("email", u.getEmail());
        response.put("tipo", u.getTipo().name());
        response.put("papeis", u.getPapeis().stream().map(Enum::name).toList());
        response.put("verificado", u.isVerificado());
        response.put("fotoUrl", u.getFotoUrl() != null ? u.getFotoUrl() : "");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<?> salvarFcmToken(Authentication auth,
                                            @Valid @RequestBody SalvarFcmTokenRequest req) {
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

    @PostMapping("/tornar-cliente")
    public ResponseEntity<Map<String, Object>> tornarCliente(Authentication auth) {
        Usuario u = repository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (u.temPapel(Usuario.TipoUsuario.CLIENTE)) {
            throw new IllegalArgumentException("Você já é cliente");
        }

        u.adicionarPapel(Usuario.TipoUsuario.CLIENTE);
        repository.save(u);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Perfil de cliente ativado!",
                "papeis", u.getPapeis().stream().map(Enum::name).toList()
        ));
    }

    @PostMapping(value = "/foto", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadFoto(Authentication auth,
                                        @RequestParam("file") MultipartFile file) {
        Usuario u = repository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        try {
            String url = uploadService.salvarFotoUsuario(u.getId(), file);
            u.setFotoUrl(url);
            repository.save(u);
            return ResponseEntity.ok(Map.of("fotoUrl", url));
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao salvar imagem: " + e.getMessage());
        }
    }
}