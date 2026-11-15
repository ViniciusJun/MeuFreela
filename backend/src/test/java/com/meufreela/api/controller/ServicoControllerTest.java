package com.meufreela.api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServicoControllerTest {

    @Autowired private MockMvc mvc;

    private String emailUnico() {
        return "u-" + UUID.randomUUID() + "@test.com";
    }

    private String cpfUnico() {
        return String.format("%011d",
                (long) (Math.random() * 100_000_000_000L));
    }

    /** Cadastra um usuário do tipo desejado e devolve o token JWT. */
    private String cadastrar(String email, String tipo) throws Exception {
        String json = """
            {"nome":"User Test","email":"%s","senha":"senha12345",
             "cpf":"%s","telefone":"11999999999","tipo":"%s"}
            """.formatted(email, cpfUnico(), tipo);

        String resp = mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(resp, "$.token");
    }

    private String servicoJson(String titulo, double preco) {
        return """
            {"titulo":"%s",
             "descricao":"Descrição completa do serviço",
             "categoria":"TECNOLOGIA",
             "preco":%s}
            """.formatted(titulo, preco);
    }

    // ------------------------------------------------------------------
    // 1. Listagem pública (sem token)
    // ------------------------------------------------------------------
    @Test
    void listar_semToken_deveRetornar200() throws Exception {
        mvc.perform(get("/api/servicos"))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------
    // 2. Publicar SEM token → 401
    // ------------------------------------------------------------------
    @Test
    void criar_semToken_deveRetornar401() throws Exception {
        mvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson("Serviço Teste", 100.0)))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------
    // 3. Publicar autenticado como freelancer → 201
    // ------------------------------------------------------------------
    @Test
    void criar_comoFreelancer_deveRetornar201() throws Exception {
        String token = cadastrar(emailUnico(), "FREELANCER");

        mvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson("Instalação de ar-condicionado", 200.0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.titulo").value("Instalação de ar-condicionado"))
                .andExpect(jsonPath("$.preco").value(200.0))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    // ------------------------------------------------------------------
    // 4. Publicar autenticado como cliente puro → 409
    // ------------------------------------------------------------------
    @Test
    void criar_comoClienteSemPapelFreelancer_deveRetornar409() throws Exception {
        String token = cadastrar(emailUnico(), "CLIENTE");

        mvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson("Serviço proibido", 100.0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem")
                        .value(org.hamcrest.Matchers.containsString("freelancer")));
    }

    // ------------------------------------------------------------------
    // 5. Publicar com dados inválidos → 400
    // ------------------------------------------------------------------
    @Test
    void criar_comTituloCurto_deveRetornar400() throws Exception {
        String token = cadastrar(emailUnico(), "FREELANCER");

        mvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson("abc", 100.0)))  // título < 5
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.titulo").exists());
    }

    // ------------------------------------------------------------------
    // 6. Publicar com preço negativo → 400
    // ------------------------------------------------------------------
    @Test
    void criar_comPrecoNegativo_deveRetornar400() throws Exception {
        String token = cadastrar(emailUnico(), "FREELANCER");

        mvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson("Serviço válido", -10.0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.preco").exists());
    }

    // ------------------------------------------------------------------
    // 7. Editar serviço de outro freelancer → 409
    // ------------------------------------------------------------------
    @Test
    void atualizar_servicoDeOutro_deveRetornar409() throws Exception {
        String tokenDono = cadastrar(emailUnico(), "FREELANCER");
        String tokenOutro = cadastrar(emailUnico(), "FREELANCER");

        // dono cria o serviço
        String resp = mvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + tokenDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson("Serviço Original", 100.0)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String servicoId = JsonPath.read(resp, "$.id");

        // outro tenta editar — payload VÁLIDO (preco >= 10) para que
        // o fluxo chegue até a checagem de dono no ServicoService.
        // Com preco=1.0, o @DecimalMin falharia antes e o teste receberia
        // 400 (validação) em vez do 409 (autorização) que está sendo testado.
        mvc.perform(put("/api/servicos/" + servicoId)
                        .header("Authorization", "Bearer " + tokenOutro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson("Hackeado", 100.0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem")
                        .value(org.hamcrest.Matchers.containsString("não é dono")));
    }

    // ------------------------------------------------------------------
    // 8. Pausar próprio serviço → 200 e ativo=false
    // ------------------------------------------------------------------
    @Test
    void alternarAtivo_servicoProprio_deveRetornar200EInverterStatus() throws Exception {
        String token = cadastrar(emailUnico(), "FREELANCER");

        String resp = mvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson("Serviço para pausar", 100.0)))
                .andReturn().getResponse().getContentAsString();

        String servicoId = JsonPath.read(resp, "$.id");

        mvc.perform(patch("/api/servicos/" + servicoId + "/ativo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }

    // ------------------------------------------------------------------
    // 9. Detalhe público → 200
    // ------------------------------------------------------------------
    @Test
    void detalhe_servicoExistente_deveRetornar200SemToken() throws Exception {
        String token = cadastrar(emailUnico(), "FREELANCER");
        String resp = mvc.perform(post("/api/servicos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson("Serviço Público", 150.0)))
                .andReturn().getResponse().getContentAsString();

        String servicoId = JsonPath.read(resp, "$.id");

        mvc.perform(get("/api/servicos/" + servicoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Serviço Público"));
    }
}