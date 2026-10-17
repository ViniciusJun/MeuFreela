package com.meufreela.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnviarMensagemRequest(
        @NotBlank(message = "Conteúdo é obrigatório")
        @Size(max = 2000, message = "Mensagem muito longa (máx. 2000 caracteres)")
        String conteudo
) {}