package com.meufreela.api.dto;

import com.meufreela.api.entity.CategoriaServico;
import jakarta.validation.constraints.*;

public record TornarFreelancerRequest(
        @NotNull(message = "Categoria é obrigatória")
        CategoriaServico categoria,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(min = 10, max = 500, message = "Descrição entre 10 e 500 caracteres")
        String descricao,

        @NotNull(message = "Preço/hora é obrigatório")
        @DecimalMin(value = "10.0", message = "Preço mínimo R$ 10/h")
        @DecimalMax(value = "1000.0", message = "Preço máximo R$ 1000/h")
        Double precoHora
) {}