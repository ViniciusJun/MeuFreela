package com.meufreela.api.dto;

import com.meufreela.api.entity.CategoriaServico;

import jakarta.validation.constraints.*;

public record CriarServicoRequest(
        @NotBlank(message = "Título é obrigatório")
        @Size(min = 5, max = 120, message = "Título entre 5 e 120 caracteres")
        String titulo,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(min = 10, max = 1000, message = "Descrição entre 10 e 1000 caracteres")
        String descricao,

        @NotNull(message = "Categoria é obrigatória")
        CategoriaServico categoria,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "10.0", message = "Preço mínimo R$ 10/h")
        @DecimalMax(value = "2000.0", message = "Preço máximo R$ 2000/h")
        Double preco
) {
}