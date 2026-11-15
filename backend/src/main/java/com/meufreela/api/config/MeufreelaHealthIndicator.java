package com.meufreela.api.config;

import com.meufreela.api.repository.UsuarioRepository;
import com.meufreela.api.repository.SolicitacaoRepository;

import lombok.RequiredArgsConstructor;

// Spring Boot 4.1.1: pacote mudou de actuate.health para health.contributor
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("meufreela")
@RequiredArgsConstructor
public class MeufreelaHealthIndicator implements HealthIndicator {

    private final UsuarioRepository usuarioRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    @Override
    public Health health() {
        try {
            long usuarios = usuarioRepository.count();
            long solicitacoes = solicitacaoRepository.count();

            return Health.up()
                    .withDetail("usuarios", usuarios)
                    .withDetail("solicitacoes", solicitacoes)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getClass().getSimpleName())
                    .build();
        }
    }
}