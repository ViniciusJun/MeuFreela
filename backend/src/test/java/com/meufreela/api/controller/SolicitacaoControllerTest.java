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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SolicitacaoControllerTest {

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

    /** Cria um serviço publicado por um freelancer e devolve o ID. */
    private String criarServico(String tokenFreelancer) throws Exception {
        String json = """
            {"titulo":"Serviço Padrão Teste",
             "descricao":"Descrição completa do serviço para teste",
             "categoria":"TECNOLOGIA","preco":100.0}
            """;
        String resp = mvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + tokenFreelancer)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resp, "$.id");
    }

    private String dataFuturaIso() {
        return LocalDateTime.now().plusDays(7)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private String dataPassadaIso() {
        return LocalDateTime.now().minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private String solicitacaoJson(String servicoId, String dataIso) {
        return """
            {"servicoId":"%s",
             "descricao":"Preciso instalar 3 tomadas na sala",
             "endereco":"Rua das Flores, 123",
             "dataDesejada":"%s",
             "duracaoHoras":3}
            """.formatted(servicoId, dataIso);
    }

    // ------------------------------------------------------------------
    // 1. Criar solicitação sem token → 401
    // ------------------------------------------------------------------
    @Test
    void criar_semToken_deveRetornar401() throws Exception {
        mvc.perform(post("/api/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitacaoJson("qualquer", dataFuturaIso())))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // 2. Criar solicitação com dados válidos → 201
    // ------------------------------------------------------------------
    @Test
    void criar_comServicoValido_deveRetornar201ComValorCalculado() throws Exception {
        String tokenFreela = cadastrar(emailUnico(), "FREELANCER");
        String servicoId = criarServico(tokenFreela);

        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");

        mvc.perform(post("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitacaoJson(servicoId, dataFuturaIso())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.valorTotal").value(300.0))  // 100×3
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    // ------------------------------------------------------------------
    // 3. Criar solicitação com data no passado → 400
    // ------------------------------------------------------------------
    @Test
    void criar_comDataPassada_deveRetornar400() throws Exception {
        String tokenFreela = cadastrar(emailUnico(), "FREELANCER");
        String servicoId = criarServico(tokenFreela);
        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");

        mvc.perform(post("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitacaoJson(servicoId, dataPassadaIso())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.dataDesejada").exists());
    }

    // ------------------------------------------------------------------
    // 4. Criar solicitação com descrição curta → 400
    // ------------------------------------------------------------------
    @Test
    void criar_comDescricaoCurta_deveRetornar400() throws Exception {
        String tokenFreela = cadastrar(emailUnico(), "FREELANCER");
        String servicoId = criarServico(tokenFreela);
        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");

        String json = """
            {"servicoId":"%s","descricao":"curta","endereco":"Rua X, 1",
             "dataDesejada":"%s","duracaoHoras":2}
            """.formatted(servicoId, dataFuturaIso());

        mvc.perform(post("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.descricao").exists());
    }

    // ------------------------------------------------------------------
    // 5. Cliente que também é freelancer contratando a si mesmo → 409
    // ------------------------------------------------------------------
    @Test
    void criar_contratandoASiMesmo_deveRetornar409() throws Exception {
        // Cadastra freelancer e "ativa" o papel de cliente
        String email = emailUnico();
        String token = cadastrar(email, "FREELANCER");
        mvc.perform(post("/api/usuarios/tornar-cliente")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String servicoId = criarServico(token);

        mvc.perform(post("/api/solicitacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitacaoJson(servicoId, dataFuturaIso())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem")
                        .value(org.hamcrest.Matchers.containsString("si mesmo")));
    }

    // ------------------------------------------------------------------
    // 6. Listar "como-cliente" sem token → 401
    // ------------------------------------------------------------------
    @Test
    void comoCliente_semToken_deveRetornar401() throws Exception {
        mvc.perform(get("/api/solicitacoes/como-cliente"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // 7. Listar "como-cliente" com token → 200
    // ------------------------------------------------------------------
    @Test
    void comoCliente_comToken_deveRetornarLista() throws Exception {
        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");
        mvc.perform(get("/api/solicitacoes/como-cliente")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ------------------------------------------------------------------
    // 8. Cliente tentando ACEITAR a própria solicitação → 409
    // ------------------------------------------------------------------
    @Test
    void atualizarStatus_clienteTentandoAceitar_deveRetornar409() throws Exception {
        String tokenFreela = cadastrar(emailUnico(), "FREELANCER");
        String servicoId = criarServico(tokenFreela);
        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");

        String resp = mvc.perform(post("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitacaoJson(servicoId, dataFuturaIso())))
                .andReturn().getResponse().getContentAsString();

        String solicitacaoId = JsonPath.read(resp, "$.id");

        mvc.perform(patch("/api/solicitacoes/" + solicitacaoId + "/status")
                        .param("status", "ACEITA")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem")
                        .value(org.hamcrest.Matchers.containsString("freelancer")));
    }

    // ------------------------------------------------------------------
    // 9. Freelancer ACEITANDO a solicitação → 200
    // ------------------------------------------------------------------
    @Test
    void atualizarStatus_freelancerAceitando_deveRetornar200() throws Exception {
        String tokenFreela = cadastrar(emailUnico(), "FREELANCER");
        String servicoId = criarServico(tokenFreela);
        String tokenCliente = cadastrar(emailUnico(), "CLIENTE");

        String resp = mvc.perform(post("/api/solicitacoes")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitacaoJson(servicoId, dataFuturaIso())))
                .andReturn().getResponse().getContentAsString();

        String solicitacaoId = JsonPath.read(resp, "$.id");

        mvc.perform(patch("/api/solicitacoes/" + solicitacaoId + "/status")
                        .param("status", "ACEITA")
                        .header("Authorization", "Bearer " + tokenFreela))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEITA"));
    }
}