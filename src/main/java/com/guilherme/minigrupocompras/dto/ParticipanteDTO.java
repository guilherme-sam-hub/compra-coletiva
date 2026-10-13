package com.guilherme.minigrupocompras.dto;

import java.time.LocalDateTime;

public record ParticipanteDTO(
        Long usuarioId,
        String nome,
        String email,
        LocalDateTime dataEntrada
) {}