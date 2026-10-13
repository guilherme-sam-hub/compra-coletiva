package com.guilherme.minigrupocompras.service;

import com.guilherme.minigrupocompras.dto.GrupoCompraResponseDTO;
import com.guilherme.minigrupocompras.dto.ParticipanteDTO;
import com.guilherme.minigrupocompras.entity.GrupoCompra;
import com.guilherme.minigrupocompras.entity.Participacao;
import com.guilherme.minigrupocompras.entity.Produto;
import com.guilherme.minigrupocompras.entity.Usuario;
import com.guilherme.minigrupocompras.enums.StatusGrupo;
import com.guilherme.minigrupocompras.exceptions.GrupoNaoEncontradoException;
import com.guilherme.minigrupocompras.repository.GrupoCompraRepository;
import com.guilherme.minigrupocompras.repository.ParticipacaoRepository;
import com.guilherme.minigrupocompras.repository.ProdutoRepository;
import com.guilherme.minigrupocompras.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class GrupoCompraService {

    private final GrupoCompraRepository grupoCompraRepository;
    private final ParticipacaoRepository participacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;

    public GrupoCompraService(
            GrupoCompraRepository grupoCompraRepository,
            ParticipacaoRepository participacaoRepository,
            UsuarioRepository usuarioRepository,
            ProdutoRepository produtoRepository
    ) {
        this.grupoCompraRepository = grupoCompraRepository;
        this.participacaoRepository = participacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public int verificarEExpirarGruposVencidos() {
        return grupoCompraRepository.expirarGruposVencidos(LocalDateTime.now());
    }

    /**
     * Endpoint lógico do botão "Quero Participar":
     * reusa o grupo ABERTO do produto ou cria um novo, e adiciona o usuário.
     */
    @Transactional
    public GrupoCompra entrarEmGrupo(Long produtoId, Long usuarioId) {
        GrupoCompra grupo = grupoCompraRepository
                .findFirstByProdutoIdAndStatusGrupoOrderByIdAsc(produtoId, StatusGrupo.ABERTO)
                .orElseGet(() -> criarNovoGrupo(produtoId));

        return adicionarParticipante(grupo.getId(), usuarioId);
    }

    private GrupoCompra criarNovoGrupo(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + produtoId));

        BigDecimal precoDesconto = produto.getPrecoOriginal()
                .multiply(new BigDecimal("0.85"))
                .setScale(2, RoundingMode.HALF_UP);

        LocalDateTime expiracao = LocalDateTime.now().plusDays(7);

        GrupoCompra novo = new GrupoCompra(
                produto,
                produto.getQuantidadeMinima(),
                precoDesconto,
                expiracao
        );
        return grupoCompraRepository.save(novo);
    }

    @Transactional
    public GrupoCompra adicionarParticipante(Long grupoId, Long usuarioId) {
        GrupoCompra grupo = grupoCompraRepository.findById(grupoId)
                .orElseThrow(() -> new GrupoNaoEncontradoException(grupoId));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + usuarioId));

        if (!grupo.podeReceberParticipantes(LocalDateTime.now())) {
            throw new IllegalStateException("Este grupo não está mais recebendo participantes.");
        }

        boolean jaParticipou = participacaoRepository
                .existsByUsuarioIdAndGrupoCompraId(usuario.getId(), grupo.getId());
        if (jaParticipou) {
            throw new IllegalStateException("Você já está participando deste grupo.");
        }

        Participacao novaParticipacao = Participacao.builder()
                .usuario(usuario)
                .grupoCompra(grupo)
                .build();
        participacaoRepository.save(novaParticipacao);

        grupo.adicionarParticipante();
        return grupo;
    }

    // ============================================================
    // DETALHES DO GRUPO (para /grupos/{id})
    // ============================================================
    @Transactional
    public GrupoCompraResponseDTO buscarDetalhes(Long grupoId, String emailLogado) {
        GrupoCompra grupo = grupoCompraRepository.findById(grupoId)
                .orElseThrow(() -> new GrupoNaoEncontradoException(grupoId));

        // Força inicialização de proxies LAZY antes de sair da transação
        grupo.getProduto().getNome();
        grupo.getParticipacoes().size();
        grupo.getParticipacoes().forEach(p -> p.getUsuario().getNome());

        List<ParticipanteDTO> participantes = grupo.getParticipacoes().stream()
                .map(p -> new ParticipanteDTO(
                        p.getUsuario().getId(),
                        p.getUsuario().getNome(),
                        p.getUsuario().getEmail(),
                        p.getDataEntrada()))
                .toList();

        boolean usuarioParticipa = participantes.stream()
                .anyMatch(p -> p.email().equalsIgnoreCase(emailLogado));

        BigDecimal precoOriginal = grupo.getProduto().getPrecoOriginal();
        BigDecimal precoDesconto = grupo.getPrecoDesconto();
        BigDecimal economia = precoOriginal.subtract(precoDesconto);

        int percentual = precoOriginal.compareTo(BigDecimal.ZERO) == 0
                ? 0
                : economia.multiply(BigDecimal.valueOf(100))
                .divide(precoOriginal, 0, RoundingMode.HALF_UP)
                .intValue();

        long diasRestantes = grupo.getDataExpiracao() != null
                ? Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), grupo.getDataExpiracao()))
                : 0;

        int faltam = Math.max(0,
                (grupo.getMetaParticipantes() != null ? grupo.getMetaParticipantes() : 0)
                        - (grupo.getQtdParticipantes() != null ? grupo.getQtdParticipantes() : 0));

        boolean podeEntrar = grupo.podeReceberParticipantes(LocalDateTime.now()) && !usuarioParticipa;

        return new GrupoCompraResponseDTO(
                grupo.getId(),
                grupo.getStatusGrupo().name(),
                grupo.getMetaParticipantes(),
                grupo.getQtdParticipantes(),
                faltam,
                precoOriginal,
                precoDesconto,
                economia,
                percentual,
                grupo.getDataExpiracao(),
                diasRestantes,
                grupo.getProduto().getId(),
                grupo.getProduto().getNome(),
                grupo.getProduto().getDescricao(),
                grupo.getProduto().getImagem(),
                participantes,
                usuarioParticipa,
                podeEntrar
        );
    }

    // ============================================================
    // SAIR DO GRUPO
    // ============================================================
    @Transactional
    public void sairDoGrupo(Long grupoId, Long usuarioId) {
        GrupoCompra grupo = grupoCompraRepository.findById(grupoId)
                .orElseThrow(() -> new GrupoNaoEncontradoException(grupoId));

        if (!grupo.podeReceberParticipantes(LocalDateTime.now())) {
            throw new IllegalStateException("Não é possível sair: o grupo não está mais aberto.");
        }

        boolean participa = participacaoRepository
                .existsByUsuarioIdAndGrupoCompraId(usuarioId, grupoId);
        if (!participa) {
            throw new IllegalStateException("Você não está participando deste grupo.");
        }

        participacaoRepository.deleteByUsuarioIdAndGrupoCompraId(usuarioId, grupoId);
        grupo.removerParticipante();
    }
}