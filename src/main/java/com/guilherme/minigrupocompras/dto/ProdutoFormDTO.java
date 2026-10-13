package com.guilherme.minigrupocompras.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoFormDTO {

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição pode ter no máximo 500 caracteres")
    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Preço inválido (máx 8 inteiros e 2 decimais)")
    private BigDecimal preco;

    @NotNull(message = "Quantidade mínima é obrigatória")
    @Min(value = 1, message = "Quantidade mínima deve ser pelo menos 1")
    private Integer quantidadeMinima;

    private String imagem; // opcional
}