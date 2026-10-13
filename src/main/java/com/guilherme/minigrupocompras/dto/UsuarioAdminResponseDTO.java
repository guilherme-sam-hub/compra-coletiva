package com.guilherme.minigrupocompras.dto;

import java.time.LocalDateTime;

public record UsuarioAdminResponseDTO(
        Long id,
        String nome,
        String cpf,
        String email,
        String role,
        Boolean ativo,
        LocalDateTime dataCadastro
) {}