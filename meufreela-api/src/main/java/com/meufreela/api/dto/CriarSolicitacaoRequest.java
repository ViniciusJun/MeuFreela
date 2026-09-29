package com.meufreela.api.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CriarSolicitacaoRequest(
        @NotBlank(message = "ID do freelancer é obrigatório")
        String freelancerId,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(min = 10, max = 1000, message = "Descrição deve ter entre 10 e 1000 caracteres")
        String descricao,

        @NotBlank(message = "Endereço é obrigatório")
        @Size(max = 300)
        String endereco,

        @NotNull(message = "Data é obrigatória")
        @Future(message = "Data deve ser no futuro")
        LocalDateTime dataDesejada,

        @NotNull(message = "Duração é obrigatória")
        @Min(value = 1, message = "Duração mínima é 1 hora")
        @Max(value = 24, message = "Duração máxima é 24 horas")
        Integer duracaoHoras
) {}