package com.meufreela.api.service;

import com.meufreela.api.dto.AvaliacaoResumo;
import com.meufreela.api.dto.CriarAvaliacaoRequest;
import com.meufreela.api.entity.Avaliacao;
import com.meufreela.api.entity.Solicitacao;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.AvaliacaoRepository;
import com.meufreela.api.repository.SolicitacaoRepository;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public AvaliacaoResumo criar(String emailAutor, CriarAvaliacaoRequest req) {
        Usuario autor = usuarioRepository.findByEmail(emailAutor)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Solicitacao s = solicitacaoRepository.findById(req.solicitacaoId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!"CONCLUIDA".equals(s.getStatus().name())
                && !"PAGA".equals(s.getStatus().name())) {
            throw new IllegalArgumentException(
                    "Só é possível avaliar após a conclusão do serviço");
        }

        boolean ehCliente = s.getCliente().getId().equals(autor.getId());
        boolean ehFreelancer = s.getFreelancer().getId().equals(autor.getId());

        if (!ehCliente && !ehFreelancer) {
            throw new IllegalArgumentException("Você não participou desta solicitação");
        }

        if (avaliacaoRepository.existsBySolicitacaoAndAutor(s, autor)) {
            throw new IllegalArgumentException("Você já avaliou esta solicitação");
        }

        Usuario avaliado = ehCliente ? s.getFreelancer() : s.getCliente();

        Avaliacao a = Avaliacao.builder()
                .solicitacao(s)
                .autor(autor)
                .avaliado(avaliado)
                .nota(req.nota())
                .comentario(req.comentario())
                .build();

        avaliacaoRepository.save(a);
        atualizarReputacao(avaliado);

        return AvaliacaoResumo.from(a);
    }

    public List<AvaliacaoResumo> doUsuario(String usuarioId) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return avaliacaoRepository.findByAvaliadoOrderByCriadoEmDesc(u)
                .stream().map(AvaliacaoResumo::from).toList();
    }

    private void atualizarReputacao(Usuario avaliado) {
        Double media = avaliacaoRepository.mediaPorAvaliado(avaliado.getId());
        Long total = avaliacaoRepository.totalPorAvaliado(avaliado.getId());

        avaliado.setAvaliacaoMedia(media != null ? Math.round(media * 10.0) / 10.0 : 0.0);
        avaliado.setTotalServicos(total != null ? total.intValue() : 0);
        usuarioRepository.save(avaliado);
    }
}