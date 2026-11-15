package com.meufreela.api.controller;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração REST do chat.
 *
 * Fluxo:
 *  1. Cadastra freelancer e cliente
 *  2. Freelancer publica um serviço
 *  3. Cliente cria solicitação sobre o serviço
 *  4. Testa GET/POST em /api/solicitacoes/{id}/mensagens
 *
 * Valida integração completa: JWT → SecurityFilterChain → Controller → Service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerTest {

    @Autowired private MockMvc mvc;

    private String emailUnico() {
        return "u-" + UUID.randomUUID() + "@test.com";
    }

    private String cpfUnico() {
        return String.format("%011d",
                (long) (Math.random() * 100_000_000_000L));
    }

    private String cadastrar(String email, String tipo) throws Exception {
        String json = """
            {"nome":"User Test","email":"%s","senha":"senha12345",
             "cpf":"%s","telefone":"11999999999","tipo":"%s"}
            """.formatted(email, cpfUnico(), tipo);
        String resp = mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resp, "$.token");
    }

    private String criarServico(String tokenFreelancer) throws Exception {
        String json = """
            {"titulo":"Serviço Chat Teste",
             "descricao":"Descrição completa para teste de chat",
             "categoria":"TECNOLOGIA","preco":100.0}
            """;
        String resp = mvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + tokenFreelancer)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resp, "$.id");
    }

    private String criarSolicitacao(String tokenCliente, String servicoId) throws Exception {
        String dataIso = LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String json = """
            {"servicoId":"%s",
             "descricao":"Preciso instalar 3 tomadas na sala",
             "endereco":"Rua das Flores, 123",
             "dataDesejada":"%s",
             "duracaoHoras":3}
            """.formatted(servicoId, dataIso);
        String resp = mvc.perform(post("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resp, "$.id");
    }

    // ==================== GET historico ====================

    @Test
    void historico_semToken_deveRetornar401() throws Exception {
        mvc.perform(get("/api/solicitacoes/sol-1/mensagens"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void historico_participante_deveRetornar200() throws Exception {
        String tokenFreela = cadastrar(emailUnico(), "FREELANCER");
        String servicoId = criarServico(tokenFreela);
        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");
        String solicitacaoId = criarSolicitacao(tokenCliente, servicoId);

        mvc.perform(get("/api/solicitacoes/" + solicitacaoId + "/mensagens")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void historico_naoParticipante_deveRetornar409() throws Exception {
        String tokenFreela = cadastrar(emailUnico(), "FREELANCER");
        String servicoId = criarServico(tokenFreela);
        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");
        String solicitacaoId = criarSolicitacao(tokenCliente, servicoId);

        // Outro usuário (sem relação com a solicitação) tenta ver as mensagens
        String tokenOutro = cadastrar(emailUnico(), "CLIENTE");

        // IllegalArgument("Você não participa") → 409 no GlobalExceptionHandler
        mvc.perform(get("/api/solicitacoes/" + solicitacaoId + "/mensagens")
                        .header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isConflict());
    }

    @Test
    void historico_solicitacaoInexistente_deveRetornar409() throws Exception {
        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");

        mvc.perform(get("/api/solicitacoes/nao-existe/mensagens")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isConflict());
    }

    // ==================== POST enviar ====================

    @Test
    void enviar_semToken_deveRetornar401() throws Exception {
        mvc.perform(post("/api/solicitacoes/sol-1/mensagens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\":\"Olá\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void enviar_conteudoValido_deveRetornar200() throws Exception {
        String tokenFreela = cadastrar(emailUnico(), "FREELANCER");
        String servicoId = criarServico(tokenFreela);
        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");
        String solicitacaoId = criarSolicitacao(tokenCliente, servicoId);

        mvc.perform(post("/api/solicitacoes/" + solicitacaoId + "/mensagens")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\":\"Olá, posso hoje às 15h?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").value("Olá, posso hoje às 15h?"))
                .andExpect(jsonPath("$.remetenteNome").value("User Test"))
                .andExpect(jsonPath("$.solicitacaoId").value(solicitacaoId));
    }

    @Test
    void enviar_conteudoVazio_deveRetornar400() throws Exception {
        String tokenFreela = cadastrar(emailUnico(), "FREELANCER");
        String servicoId = criarServico(tokenFreela);
        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");
        String solicitacaoId = criarSolicitacao(tokenCliente, servicoId);

        mvc.perform(post("/api/solicitacoes/" + solicitacaoId + "/mensagens")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.conteudo").exists());
    }
}