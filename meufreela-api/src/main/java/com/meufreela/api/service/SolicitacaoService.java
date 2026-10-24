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
import java.util.Map;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    @Transactional
    public SolicitacaoResumo criar(String emailCliente, CriarSolicitacaoRequest req) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        if (!cliente.temPapel(Usuario.TipoUsuario.CLIENTE)) {
            throw new IllegalArgumentException("Você não tem perfil de cliente");
        }

        Usuario freelancer = usuarioRepository.findById(req.freelancerId())
                .orElseThrow(() -> new IllegalArgumentException("Freelancer não encontrado"));

        if (!freelancer.temPapel(Usuario.TipoUsuario.FREELANCER)) {
            throw new IllegalArgumentException("Usuário não oferece serviços");
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
        notificacaoService.enviar(
                freelancer,
                "Nova solicitação recebida",
                cliente.getNome() + " quer contratar você: " + req.descricao(),
                Map.of("tipo", "NOVA_SOLICITACAO", "solicitacaoId", s.getId())
        );
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
        String texto;
        switch (novoStatus) {
            case ACEITA -> texto = "Sua solicitação foi aceita!";
            case RECUSADA -> texto = "Sua solicitação foi recusada.";
            case CONCLUIDA -> texto = "Serviço concluído. Avalie o profissional!";
            case CANCELADA -> texto = "A solicitação foi cancelada.";
            default -> texto = "Status atualizado: " + novoStatus;
        }

        notificacaoService.enviar(
                s.getCliente(),
                "Atualização da solicitação",
                texto,
                Map.of("tipo", "STATUS_ALTERADO",
                        "solicitacaoId", s.getId(),
                        "status", novoStatus.name())
        );
        return SolicitacaoResumo.from(s);
    }
}