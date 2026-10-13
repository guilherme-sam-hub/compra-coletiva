package com.guilherme.minigrupocompras.repository;

import com.guilherme.minigrupocompras.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Método para verificar se valor já existe
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);

    // Método para buscar por email (login)
    Optional<Usuario> findByEmail(String email);



}
