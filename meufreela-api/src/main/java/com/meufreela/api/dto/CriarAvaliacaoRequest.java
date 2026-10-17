package com.meufreela.api.dto;

import jakarta.validation.constraints.*;

public record CriarAvaliacaoRequest(
        @NotBlank(message = "ID da solicitação é obrigatório")
        String solicitacaoId,

        @NotNull(message = "Nota é obrigatória")
        @Min(value = 1, message = "Nota mínima é 1")
        @Max(value = 5, message = "Nota máxima é 5")
        Integer nota,

        @Size(max = 500, message = "Comentário deve ter até 500 caracteres")
        String comentario
) {}