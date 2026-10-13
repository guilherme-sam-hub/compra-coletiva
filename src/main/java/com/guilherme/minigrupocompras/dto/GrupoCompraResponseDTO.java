package com.guilherme.minigrupocompras.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GrupoCompraResponseDTO(
        Long id,
        String status,
        Integer metaParticipantes,
        Integer qtdParticipantes,
        Integer faltam,

        BigDecimal precoOriginal,
        BigDecimal precoDesconto,
        BigDecimal economia,
        Integer percentualDesconto,

        LocalDateTime dataExpiracao,
        long diasRestantes,

        Long produtoId,
        String produtoNome,
        String produtoDescricao,
        String produtoImagem,

        List<ParticipanteDTO> participantes,
        boolean usuarioParticipa,
        boolean usuarioPodeEntrar
) {}