package com.meufreela.api.service;

import com.meufreela.api.dto.CriarServicoRequest;
import com.meufreela.api.dto.ServicoResumo;
import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Servico;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.ServicoRepository;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.meufreela.api.exception.OperacaoNaoPermitidaException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ServicoResumo criar(String email, CriarServicoRequest req) {
        Usuario freelancer = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!freelancer.temPapel(Usuario.TipoUsuario.FREELANCER)) {
            throw new IllegalArgumentException(
                    "Ative seu perfil de freelancer antes de publicar serviços");
        }

        Servico s = Servico.builder()
                .freelancer(freelancer)
                .titulo(req.titulo())
                .descricao(req.descricao())
                .categoria(req.categoria())
                .preco(req.preco())
                .build();

        servicoRepository.save(s);
        return ServicoResumo.from(s);
    }

    public List<ServicoResumo> listar(CategoriaServico categoria) {
        List<Servico> servicos = (categoria != null)
                ? servicoRepository.findByAtivoTrueAndCategoriaOrderByCriadoEmDesc(categoria)
                : servicoRepository.findByAtivoTrueOrderByCriadoEmDesc();
        return servicos.stream().map(ServicoResumo::from).toList();
    }

    public List<ServicoResumo> meus(String email) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return servicoRepository.findByFreelancerOrderByCriadoEmDesc(u)
                .stream().map(ServicoResumo::from).toList();
    }

    public ServicoResumo detalhe(String id) {
        return servicoRepository.findById(id)
                .map(ServicoResumo::from)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
    }

    @Transactional
    public ServicoResumo atualizar(String email, String id, CriarServicoRequest req) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Servico s = servicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        if (!s.getFreelancer().getId().equals(u.getId())) {
            throw new OperacaoNaoPermitidaException("Você não é dono deste serviço");
        }

        s.setTitulo(req.titulo());
        s.setDescricao(req.descricao());
        s.setCategoria(req.categoria());
        s.setPreco(req.preco());
        servicoRepository.save(s);

        return ServicoResumo.from(s);
    }

    @Transactional
    public ServicoResumo alternarAtivo(String email, String id) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Servico s = servicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        if (!s.getFreelancer().getId().equals(u.getId())) {
            throw new OperacaoNaoPermitidaException("Você não é dono deste serviço");
        }

        s.setAtivo(!s.isAtivo());
        servicoRepository.save(s);
        return ServicoResumo.from(s);
    }

    @Transactional
    public void excluir(String email, String id) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Servico s = servicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        if (!s.getFreelancer().getId().equals(u.getId())) {
            throw new OperacaoNaoPermitidaException("Você não é dono deste serviço");
        }

        servicoRepository.delete(s);
    }
}