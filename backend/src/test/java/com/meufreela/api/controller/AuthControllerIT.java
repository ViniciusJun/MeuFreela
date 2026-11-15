package com.meufreela.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired private MockMvc mvc;

    private String emailUnico() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    private String cpfUnico() {
        long n = (long) (Math.random() * 100_000_000_000L);
        return String.format("%011d", n);
    }

    private String cadastroJson(String email, String cpf) {
        return """
            {
              "nome": "João Test",
              "email": "%s",
              "senha": "senha12345",
              "cpf": "%s",
              "telefone": "11999999999",
              "tipo": "CLIENTE"
            }
            """.formatted(email, cpf);
    }

    @Test
    void cadastro_comDadosValidos_deveRetornar201EToken() throws Exception {
        String email = emailUnico();
        String cpf = cpfUnico();

        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson(email, cpf)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.usuario.email").value(email))
                .andExpect(jsonPath("$.usuario.tipo").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.papeis[0]").value("CLIENTE"));
    }

    @Test
    void cadastro_comEmailDuplicado_deveRetornar409() throws Exception {
        String email = emailUnico();
        String cpf1 = cpfUnico();
        String cpf2 = cpfUnico();

        // primeiro cadastro
        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson(email, cpf1)))
                .andExpect(status().isCreated());

        // segundo cadastro com o mesmo email
        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson(email, cpf2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void login_comCredenciaisValidas_deveRetornar200() throws Exception {
        String email = emailUnico();
        String cpf = cpfUnico();

        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson(email, cpf)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email": "%s", "senha": "senha12345"}
                    """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_comSenhaErrada_deveRetornar401() throws Exception {
        String email = emailUnico();
        String cpf = cpfUnico();

        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson(email, cpf)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email": "%s", "senha": "errada"}
                    """.formatted(email)))
                .andExpect(status().isUnauthorized());
    }
}