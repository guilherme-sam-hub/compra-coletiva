package com.guilherme.minigrupocompras.repository;

import com.guilherme.minigrupocompras.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Herda do JpaRepository: save, findAll, findById, delete, count, exists...
// <Produto, Long> = <Entidade, Tipo da Chave Primária>
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}