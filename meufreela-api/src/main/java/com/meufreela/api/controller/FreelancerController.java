package com.meufreela.api.controller;

import com.meufreela.api.dto.FreelancerResumo;
import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/freelancers")
@RequiredArgsConstructor
public class FreelancerController {

    private final UsuarioRepository repository;

    @GetMapping
    public ResponseEntity<List<FreelancerResumo>> listar(
            @RequestParam(required = false) CategoriaServico categoria) {

        List<Usuario> freelancers = (categoria != null)
                ? repository.findByTipoAndCategoria(Usuario.TipoUsuario.FREELANCER, categoria)
                : repository.findByTipo(Usuario.TipoUsuario.FREELANCER);

        return ResponseEntity.ok(
                freelancers.stream().map(FreelancerResumo::from).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<FreelancerResumo> detalhe(@PathVariable String id) {
        return repository.findById(id)
                .filter(u -> u.getTipo() == Usuario.TipoUsuario.FREELANCER)
                .map(u -> ResponseEntity.ok(FreelancerResumo.from(u)))
                .orElse(ResponseEntity.notFound().build());
    }
}