package com.meufreela.api.service;

import com.meufreela.api.dto.MensagemResumo;
import com.meufreela.api.entity.Mensagem;
import com.meufreela.api.entity.Solicitacao;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.MensagemRepository;
import com.meufreela.api.repository.SolicitacaoRepository;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MensagemRepository mensagemRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public MensagemResumo salvar(String emailRemetente, String solicitacaoId, String conteudo) {
        Usuario remetente = usuarioRepository.findByEmail(emailRemetente)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Solicitacao s = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        boolean ehParte = s.getCliente().getId().equals(remetente.getId())
                || s.getFreelancer().getId().equals(remetente.getId());

        if (!ehParte) {
            throw new IllegalArgumentException("Você não participa desta conversa");
        }

        Mensagem m = Mensagem.builder()
                .solicitacao(s)
                .remetente(remetente)
                .conteudo(conteudo)
                .build();

        mensagemRepository.save(m);
        return MensagemResumo.from(m);
    }

    public List<MensagemResumo> historico(String emailSolicitante, String solicitacaoId) {
        Usuario u = usuarioRepository.findByEmail(emailSolicitante)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Solicitacao s = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        boolean ehParte = s.getCliente().getId().equals(u.getId())
                || s.getFreelancer().getId().equals(u.getId());

        if (!ehParte) {
            throw new IllegalArgumentException("Sem permissão para ver esta conversa");
        }

        return mensagemRepository.findBySolicitacaoOrderByEnviadaEmAsc(s)
                .stream().map(MensagemResumo::from).toList();
    }
}