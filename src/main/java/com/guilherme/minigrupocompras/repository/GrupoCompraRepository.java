package com.guilherme.minigrupocompras.repository;

import com.guilherme.minigrupocompras.entity.GrupoCompra;
import com.guilherme.minigrupocompras.enums.StatusGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface GrupoCompraRepository extends JpaRepository<GrupoCompra, Long> {

    @Modifying
    @Transactional
    @Query(
            "UPDATE GrupoCompra g " +
                    "SET g.statusGrupo = 'EXPIRADO' " +
                    "WHERE g.statusGrupo = 'ABERTO' " +
                    "AND g.dataExpiracao < :agora"
    )
    int expirarGruposVencidos(@Param("agora") LocalDateTime agora);

    /**
     * Busca o grupo ABERTO mais antigo de um produto.
     * Usado pelo "Quero Participar": se existir, o usuário entra nele;
     * se não existir, o service cria um novo.
     */
    Optional<GrupoCompra> findFirstByProdutoIdAndStatusGrupoOrderByIdAsc(
            Long produtoId, StatusGrupo statusGrupo);
}