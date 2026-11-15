package com.meufreela.api.service;

import com.meufreela.api.dto.CriarAvaliacaoRequest;
import com.meufreela.api.entity.Avaliacao;
import com.meufreela.api.entity.Solicitacao;
import com.meufreela.api.entity.StatusSolicitacao;
import com.meufreela.api.entity.Usuario;
import com.meufreela.api.repository.AvaliacaoRepository;
import com.meufreela.api.repository.SolicitacaoRepository;
import com.meufreela.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private SolicitacaoRepository solicitacaoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private AvaliacaoService avaliacaoService;

    private Usuario cliente;
    private Usuario freelancer;
    private Solicitacao solicitacao;

    @BeforeEach
    void setUp() {
        cliente = Usuario.builder()
                .id("cli-1")
                .nome("João Cliente")
                .email("joao@test.com")
                .tipo(Usuario.TipoUsuario.CLIENTE)
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.CLIENTE)))
                .build();

        freelancer = Usuario.builder()
                .id("fre-1")
                .nome("Carlos Eletricista")
                .email("carlos@test.com")
                .tipo(Usuario.TipoUsuario.FREELANCER)
                .papeis(new HashSet<>(Set.of(Usuario.TipoUsuario.FREELANCER)))
                .avaliacaoMedia(0.0)
                .totalServicos(0)
                .build();

        solicitacao = Solicitacao.builder()
                .id("sol-1")
                .cliente(cliente)
                .freelancer(freelancer)
                .status(StatusSolicitacao.CONCLUIDA)  // padrão dos testes
                .build();
    }

    private CriarAvaliacaoRequest request(int nota) {
        return new CriarAvaliacaoRequest("sol-1", nota, "Excelente serviço!");
    }

    // =================================================================
    // TESTE 1: Cliente avaliando freelancer com sucesso
    // =================================================================
    @Test
    void criar_clienteAvaliandoFreelancer_deveSalvarERecalcularMedia() {
        when(usuarioRepository.findByEmail("joao@test.com"))
                .thenReturn(Optional.of(cliente));
        when(solicitacaoRepository.findById("sol-1"))
                .thenReturn(Optional.of(solicitacao));
        when(avaliacaoRepository.existsBySolicitacaoAndAutor(solicitacao, cliente))
                .thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class)))
                .thenAnswer(inv -> {
                    Avaliacao a = inv.getArgument(0);
                    a.setId("av-1");
                    return a;
                });

        // Simula média após a avaliação
        when(avaliacaoRepository.mediaPorAvaliado("fre-1")).thenReturn(5.0);
        when(avaliacaoRepository.totalPorAvaliado("fre-1")).thenReturn(1L);

        var resp = avaliacaoService.criar("joao@test.com", request(5));

        assertThat(resp.nota()).isEqualTo(5);
        assertThat(resp.autorId()).isEqualTo("cli-1");
        assertThat(resp.avaliadoId()).isEqualTo("fre-1");

        // Reputação do freelancer foi atualizada
        assertThat(freelancer.getAvaliacaoMedia()).isEqualTo(5.0);
        assertThat(freelancer.getTotalServicos()).isEqualTo(1);

        verify(avaliacaoRepository).save(any(Avaliacao.class));
        verify(usuarioRepository).save(freelancer);
    }

    // =================================================================
    // TESTE 2: Freelancer avaliando cliente (bilateral)
    // =================================================================
    @Test
    void criar_freelancerAvaliandoCliente_deveSalvarERecalcularMediaDoCliente() {
        when(usuarioRepository.findByEmail("carlos@test.com"))
                .thenReturn(Optional.of(freelancer));
        when(solicitacaoRepository.findById("sol-1"))
                .thenReturn(Optional.of(solicitacao));
        when(avaliacaoRepository.existsBySolicitacaoAndAutor(solicitacao, freelancer))
                .thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class)))
                .thenAnswer(inv -> {
                    Avaliacao a = inv.getArgument(0);
                    a.setId("av-2");
                    return a;
                });

        when(avaliacaoRepository.mediaPorAvaliado("cli-1")).thenReturn(4.5);
        when(avaliacaoRepository.totalPorAvaliado("cli-1")).thenReturn(1L);

        var resp = avaliacaoService.criar("carlos@test.com", request(4));

        assertThat(resp.autorId()).isEqualTo("fre-1");
        assertThat(resp.avaliadoId()).isEqualTo("cli-1");   // cliente é o avaliado
        assertThat(cliente.getAvaliacaoMedia()).isEqualTo(4.5);
        assertThat(cliente.getTotalServicos()).isEqualTo(1);
    }

    // =================================================================
    // TESTE 3: Só pode avaliar se CONCLUIDA ou PAGA
    // =================================================================
    @Test
    void criar_servicoNaoConcluido_deveLancarExcecao() {
        solicitacao.setStatus(StatusSolicitacao.PENDENTE);

        when(usuarioRepository.findByEmail("joao@test.com"))
                .thenReturn(Optional.of(cliente));
        when(solicitacaoRepository.findById("sol-1"))
                .thenReturn(Optional.of(solicitacao));

        assertThatThrownBy(() -> avaliacaoService.criar("joao@test.com", request(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("após a conclusão");

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void criar_servicoPago_devePermitirAvaliacao() {
        solicitacao.setStatus(StatusSolicitacao.PAGA);   // PAGA também permite

        when(usuarioRepository.findByEmail("joao@test.com"))
                .thenReturn(Optional.of(cliente));
        when(solicitacaoRepository.findById("sol-1"))
                .thenReturn(Optional.of(solicitacao));
        when(avaliacaoRepository.existsBySolicitacaoAndAutor(solicitacao, cliente))
                .thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class)))
                .thenAnswer(inv -> {
                    Avaliacao a = inv.getArgument(0);
                    a.setId("av-3");
                    return a;
                });
        when(avaliacaoRepository.mediaPorAvaliado("fre-1")).thenReturn(5.0);
        when(avaliacaoRepository.totalPorAvaliado("fre-1")).thenReturn(1L);

        var resp = avaliacaoService.criar("joao@test.com", request(5));

        assertThat(resp.nota()).isEqualTo(5);
        verify(avaliacaoRepository).save(any());
    }

    // =================================================================
    // TESTE 4: Só participantes avaliam
    // =================================================================
    @Test
    void criar_usuarioNaoParticipante_deveLancarExcecao() {
        Usuario terceiro = Usuario.builder()
                .id("out-1")
                .email("intruso@test.com")
                .build();

        when(usuarioRepository.findByEmail("intruso@test.com"))
                .thenReturn(Optional.of(terceiro));
        when(solicitacaoRepository.findById("sol-1"))
                .thenReturn(Optional.of(solicitacao));

        assertThatThrownBy(() -> avaliacaoService.criar("intruso@test.com", request(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não participou");

        verify(avaliacaoRepository, never()).save(any());
    }

    // =================================================================
    // TESTE 5: Não pode avaliar duas vezes
    // =================================================================
    @Test
    void criar_avaliacaoDuplicada_deveLancarExcecao() {
        when(usuarioRepository.findByEmail("joao@test.com"))
                .thenReturn(Optional.of(cliente));
        when(solicitacaoRepository.findById("sol-1"))
                .thenReturn(Optional.of(solicitacao));
        when(avaliacaoRepository.existsBySolicitacaoAndAutor(solicitacao, cliente))
                .thenReturn(true);   // já avaliou

        assertThatThrownBy(() -> avaliacaoService.criar("joao@test.com", request(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já avaliou");

        verify(avaliacaoRepository, never()).save(any());
    }

    // =================================================================
    // TESTE 6: Média calculada corretamente (arredondamento)
    // =================================================================
    @Test
    void criar_deveArredondarMediaParaUmaCasa() {
        when(usuarioRepository.findByEmail("joao@test.com"))
                .thenReturn(Optional.of(cliente));
        when(solicitacaoRepository.findById("sol-1"))
                .thenReturn(Optional.of(solicitacao));
        when(avaliacaoRepository.existsBySolicitacaoAndAutor(solicitacao, cliente))
                .thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Média 4.6666... deve virar 4.7
        when(avaliacaoRepository.mediaPorAvaliado("fre-1")).thenReturn(4.666666);
        when(avaliacaoRepository.totalPorAvaliado("fre-1")).thenReturn(3L);

        avaliacaoService.criar("joao@test.com", request(5));

        assertThat(freelancer.getAvaliacaoMedia()).isEqualTo(4.7);
        assertThat(freelancer.getTotalServicos()).isEqualTo(3);
    }

    // =================================================================
    // TESTE 7: Solicitação não encontrada
    // =================================================================
    @Test
    void criar_solicitacaoInexistente_deveLancarExcecao() {
        when(usuarioRepository.findByEmail("joao@test.com"))
                .thenReturn(Optional.of(cliente));
        when(solicitacaoRepository.findById("sol-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.criar("joao@test.com", request(5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solicitação não encontrada");
    }

    // =================================================================
    // TESTE 8: Listar avaliações de um usuário
    // =================================================================
    @Test
    void doUsuario_deveRetornarAvaliacoesDoAvaliado() {
        Avaliacao av = Avaliacao.builder()
                .id("av-1")
                .solicitacao(solicitacao)
                .autor(cliente)
                .avaliado(freelancer)
                .nota(5)
                .comentario("Perfeito!")
                .build();

        when(usuarioRepository.findById("fre-1"))
                .thenReturn(Optional.of(freelancer));
        when(avaliacaoRepository.findByAvaliadoOrderByCriadoEmDesc(freelancer))
                .thenReturn(java.util.List.of(av));

        var resp = avaliacaoService.doUsuario("fre-1");

        assertThat(resp).hasSize(1);
        assertThat(resp.get(0).nota()).isEqualTo(5);
        assertThat(resp.get(0).autorNome()).isEqualTo("João Cliente");
    }
}