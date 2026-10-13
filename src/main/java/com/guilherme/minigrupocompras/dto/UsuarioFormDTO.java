package com.guilherme.minigrupocompras.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Gera getters, setters, toString, equals e hashCode (ideal para forms do Thymeleaf)
@NoArgsConstructor // Obrigatório para o Thymeleaf instanciar o objeto vazio
@AllArgsConstructor // Útil para testes ou conversão manual
public class UsuarioFormDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$",
            message = "CPF inválido (use 000.000.000-00 ou 11 dígitos)")
    private String cpf;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Formato de email inválido")
    @Size(max = 150, message = "Email muito longo")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
    private String senha;
}