package com.meufreela.api.service;

import com.meufreela.api.dto.CriarSolicitacaoRequest;
import com.meufreela.api.dto.SolicitacaoResumo;
import com.meufreela.api.entity.Solicitacao;
import com.meufreela.api.entity.StatusSolicitacao;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.SolicitacaoRepository;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public SolicitacaoResumo criar(String emailCliente, CriarSolicitacaoRequest req) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        if (cliente.getTipo() != Usuario.TipoUsuario.CLIENTE) {
            throw new IllegalArgumentException("Apenas clientes podem criar solicitações");
        }

        Usuario freelancer = usuarioRepository.findById(req.freelancerId())
                .orElseThrow(() -> new IllegalArgumentException("Freelancer não encontrado"));

        if (freelancer.getTipo() != Usuario.TipoUsuario.FREELANCER) {
            throw new IllegalArgumentException("Usuário não é um freelancer");
        }

        if (freelancer.getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Você não pode contratar a si mesmo");
        }

        Double precoHora = freelancer.getPrecoHora() != null ? freelancer.getPrecoHora() : 0.0;
        Double valorTotal = precoHora * req.duracaoHoras();

        Solicitacao s = Solicitacao.builder()
                .cliente(cliente)
                .freelancer(freelancer)
                .descricao(req.descricao())
                .endereco(req.endereco())
                .dataDesejada(req.dataDesejada())
                .duracaoHoras(req.duracaoHoras())
                .valorTotal(valorTotal)
                .status(StatusSolicitacao.PENDENTE)
                .build();

        solicitacaoRepository.save(s);
        return SolicitacaoResumo.from(s);
    }

    public List<SolicitacaoResumo> minhasComoCliente(String email) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return solicitacaoRepository.findByClienteOrderByCriadoEmDesc(u)
                .stream().map(SolicitacaoResumo::from).toList();
    }

    public List<SolicitacaoResumo> minhasComoFreelancer(String email) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return solicitacaoRepository.findByFreelancerOrderByCriadoEmDesc(u)
                .stream().map(SolicitacaoResumo::from).toList();
    }

    @Transactional
    public SolicitacaoResumo atualizarStatus(String email, String id, StatusSolicitacao novoStatus) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Solicitacao s = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        // Autorização: só o freelancer pode aceitar/recusar/iniciar/concluir
        //                só o cliente pode cancelar
        boolean ehFreelancer = s.getFreelancer().getId().equals(u.getId());
        boolean ehCliente = s.getCliente().getId().equals(u.getId());

        if (!ehFreelancer && !ehCliente) {
            throw new IllegalArgumentException("Você não tem permissão sobre esta solicitação");
        }

        // Regras de transição de estado
        switch (novoStatus) {
            case ACEITA, RECUSADA, EM_ANDAMENTO, CONCLUIDA -> {
                if (!ehFreelancer) throw new IllegalArgumentException(
                        "Apenas o freelancer pode alterar para " + novoStatus);
            }
            case CANCELADA -> {
                if (!ehCliente) throw new IllegalArgumentException(
                        "Apenas o cliente pode cancelar");
            }
            default -> throw new IllegalArgumentException("Status inválido");
        }

        s.setStatus(novoStatus);
        s.setAtualizadoEm(LocalDateTime.now());
        solicitacaoRepository.save(s);
        return SolicitacaoResumo.from(s);
    }
}