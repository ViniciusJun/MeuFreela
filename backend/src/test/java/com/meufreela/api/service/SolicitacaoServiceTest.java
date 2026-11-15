package com.meufreela.api.service;

import com.meufreela.api.dto.CriarSolicitacaoRequest;
import com.meufreela.api.dto.SolicitacaoResumo;
import com.meufreela.api.entity.Solicitacao;
import com.meufreela.api.entity.StatusSolicitacao;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.SolicitacaoRepository;
import com.meufreela.api.repository.UsuarioRepository;
import com.meufreela.api.entity.Servico;
import com.meufreela.api.repository.ServicoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacaoServiceTest {

    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;
    private final ServicoRepository servicoRepository;

    @Transactional
    public SolicitacaoResumo criar(String emailCliente, CriarSolicitacaoRequest req) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        if (!cliente.temPapel(Usuario.TipoUsuario.CLIENTE)) {
            throw new IllegalArgumentException("Você não tem perfil de cliente");
        }

        Usuario freelancer;
        Servico servico = null;

        if (req.servicoId() != null && !req.servicoId().isBlank()) {
            servico = servicoRepository.findById(req.servicoId())
                    .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
            if (!servico.isAtivo()) {
                throw new IllegalArgumentException("Este serviço está pausado");
            }
            freelancer = servico.getFreelancer();
        } else if (req.freelancerId() != null && !req.freelancerId().isBlank()) {
            freelancer = usuarioRepository.findById(req.freelancerId())
                    .orElseThrow(() -> new IllegalArgumentException("Freelancer não encontrado"));
        } else {
            // CORREÇÃO: mensagem alinhada ao contrato esperado pelo teste.
            throw new IllegalArgumentException("Informe o freelancer ou o serviço");
        }

        if (!freelancer.temPapel(Usuario.TipoUsuario.FREELANCER)) {
            throw new IllegalArgumentException("Usuário não oferece serviços");
        }

        if (freelancer.getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Você não pode contratar a si mesmo");
        }

        // CORREÇÃO (regra de negócio): quando a contratação é via serviço,
        // o preço unitário vem do serviço. Caso contrário, do perfil do freelancer.
        double precoHora;
        if (servico != null && servico.getPreco() != null) {
            precoHora = servico.getPreco();
        } else if (freelancer.getPrecoHora() != null) {
            precoHora = freelancer.getPrecoHora();
        } else {
            precoHora = 0.0;
        }
        double valorTotal = precoHora * req.duracaoHoras();

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
        notificacaoService.enviar(freelancer, "Nova solicitação recebida",
                cliente.getNome() + " quer contratar você: " + req.descricao(),
                Map.of("tipo", "NOVA_SOLICITACAO", "solicitacaoId", s.getId()));
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

        boolean ehFreelancer = s.getFreelancer().getId().equals(u.getId());
        boolean ehCliente = s.getCliente().getId().equals(u.getId());

        if (!ehFreelancer && !ehCliente) {
            throw new IllegalArgumentException("Você não tem permissão sobre esta solicitação");
        }

        switch (novoStatus) {
            case ACEITA, RECUSADA, EM_ANDAMENTO, CONCLUIDA -> {
                if (!ehFreelancer)
                    throw new IllegalArgumentException("Apenas o freelancer pode alterar para " + novoStatus);
            }
            case CANCELADA -> {
                if (!ehCliente)
                    throw new IllegalArgumentException("Apenas o cliente pode cancelar");
            }
            default -> throw new IllegalArgumentException("Status inválido");
        }

        s.setStatus(novoStatus);
        s.setAtualizadoEm(LocalDateTime.now());
        solicitacaoRepository.save(s);

        String texto;
        switch (novoStatus) {
            case ACEITA -> texto = "Sua solicitação foi aceita!";
            case RECUSADA -> texto = "Sua solicitação foi recusada.";
            case CONCLUIDA -> texto = "Serviço concluído. Avalie o profissional!";
            case CANCELADA -> texto = "A solicitação foi cancelada.";
            default -> texto = "Status atualizado: " + novoStatus;
        }

        notificacaoService.enviar(s.getCliente(), "Atualização da solicitação", texto,
                Map.of("tipo", "STATUS_ALTERADO",
                        "solicitacaoId", s.getId(),
                        "status", novoStatus.name()));
        return SolicitacaoResumo.from(s);
    }
}