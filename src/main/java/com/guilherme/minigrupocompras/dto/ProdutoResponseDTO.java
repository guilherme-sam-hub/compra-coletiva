package com.guilherme.minigrupocompras.dto;

import java.math.BigDecimal;

// Record: imutável, ideal para resposta de API/Thymeleaf
public record ProdutoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer quantidadeMinima,
        String imagem,
        Boolean ativo
) {}