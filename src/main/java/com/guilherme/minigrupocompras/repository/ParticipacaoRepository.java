package com.guilherme.minigrupocompras.repository;

import com.guilherme.minigrupocompras.entity.Participacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ParticipacaoRepository extends JpaRepository<Participacao, Long> {

    boolean existsByUsuarioIdAndGrupoCompraId(Long usuarioId, Long grupoCompraId);

    @Modifying
    @Transactional
    void deleteByUsuarioIdAndGrupoCompraId(Long usuarioId, Long grupoCompraId);
}