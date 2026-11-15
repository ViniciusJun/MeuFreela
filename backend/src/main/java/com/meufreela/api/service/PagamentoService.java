package com.meufreela.api.service;

import com.meufreela.api.dto.PagamentoResumo;
import com.meufreela.api.entity.*;
import com.meufreela.api.exception.OperacaoNaoPermitidaException;
import com.meufreela.api.repository.PagamentoRepository;
import com.meufreela.api.repository.SolicitacaoRepository;
import com.meufreela.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Simula a criação de um Payment Intent no Stripe/gateway.
     * O dinheiro entra em estado RETIDO (Escrow).
     */
    @Transactional
    public PagamentoResumo processarPagamento(String emailCliente, String solicitacaoId) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        Solicitacao s = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!s.getCliente().getId().equals(cliente.getId())) {
            throw new OperacaoNaoPermitidaException("Você não é o cliente desta solicitação");
        }

        if (s.getPagamento() != null && s.getPagamento().getStatus() == StatusPagamento.RETIDO) {
            throw new IllegalArgumentException("Solicitação já foi paga e está retida em escrow");
        }

        // Simulação de Gateway: Gera um ID de transação
        String fakeGatewayId = "txn_" + UUID.randomUUID().toString();

        Pagamento pagamento = Pagamento.builder()
                .solicitacao(s)
                .valor(s.getValorTotal())
                .status(StatusPagamento.RETIDO)
                .gatewayTransactionId(fakeGatewayId)
                .build();

        pagamentoRepository.save(pagamento);
        s.setPagamento(pagamento);

        // Neste ponto o dinheiro está com a plataforma.
        solicitacaoRepository.save(s);

        return PagamentoResumo.from(pagamento);
    }

    /**
     * Libera o dinheiro para o freelancer após o cliente aprovar a conclusão.
     */
    @Transactional
    public PagamentoResumo liberarEscrow(String emailCliente, String solicitacaoId) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        Solicitacao s = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!s.getCliente().getId().equals(cliente.getId())) {
            throw new OperacaoNaoPermitidaException("Apenas o cliente pode liberar o pagamento");
        }

        Pagamento p = s.getPagamento();
        if (p == null || p.getStatus() != StatusPagamento.RETIDO) {
            throw new IllegalArgumentException("Não há pagamento retido para ser liberado");
        }

        if (s.getStatus() != StatusSolicitacao.CONCLUIDA) {
            throw new IllegalArgumentException("O serviço ainda não foi concluído pelo freelancer");
        }

        p.setStatus(StatusPagamento.LIBERADO);
        p.setLiberadoEm(LocalDateTime.now());
        pagamentoRepository.save(p);

        s.setStatus(StatusSolicitacao.PAGA);
        solicitacaoRepository.save(s);

        return PagamentoResumo.from(p);
    }
}