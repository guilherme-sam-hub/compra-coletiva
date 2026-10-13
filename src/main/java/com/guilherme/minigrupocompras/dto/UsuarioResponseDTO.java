package com.guilherme.minigrupocompras.dto;

/**
 * DTO for {@link com.guilherme.minigrupocompras.entity.Usuario}
 */
public record UsuarioResponseDTO(Long id, String nome, String cpf, String email) {
}