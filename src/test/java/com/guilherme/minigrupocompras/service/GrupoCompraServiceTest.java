package com.guilherme.minigrupocompras.service;

import com.guilherme.minigrupocompras.dto.GrupoCompraResponseDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrupoCompraServiceTest {

    @Mock private GrupoCompraRepository grupoCompraRepository;
    @Mock private ParticipacaoRepository participacaoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProdutoRepository produtoRepository;

    @InjectMocks
    private GrupoCompraService grupoCompraService;

    private Produto produto;
    private Usuario usuario;
    private GrupoCompra grupo;

    @BeforeEach
    void setUp() {
        produto = Produto.builder()
                .nome("SSD NVMe")
                .descricao("desc")
                .precoOriginal(new BigDecimal("500.00"))
                .quantidadeMinima(10)
                .ativo(true)
                .build();
        setId(produto, 1L);

        usuario = Usuario.builder()
                .nome("Maria").cpf("12345678901")
                .email("maria@teste.com").senha("hash").ativo(true)
                .build();
        setId(usuario, 10L);

        grupo = new GrupoCompra(produto, 10, new BigDecimal("425.00"),
                LocalDateTime.now().plusDays(7));
        setId(grupo, 100L);
        setQtdParticipantes(grupo, 0);
        setParticipacoes(grupo, new ArrayList<>());
    }

    // ============================================================
    // ENTRAR EM GRUPO (cria novo se não existir)
    // ============================================================

    @Test
    @DisplayName("entrarEmGrupo: deve criar novo grupo quando não existe ABERTO")
    void entrarEmGrupo_DeveCriarNovoGrupo() {
        when(grupoCompraRepository
                .findFirstByProdutoIdAndStatusGrupoOrderByIdAsc(1L, StatusGrupo.ABERTO))
                .thenReturn(Optional.empty());
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(grupoCompraRepository.save(any(GrupoCompra.class))).thenAnswer(inv -> {
            GrupoCompra g = inv.getArgument(0);
            setId(g, 100L);
            setParticipacoes(g, new ArrayList<>());
            return g;
        });
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(participacaoRepository.existsByUsuarioIdAndGrupoCompraId(10L, 100L))
                .thenReturn(false);
        when(participacaoRepository.save(any(Participacao.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(grupoCompraRepository.findById(100L)).thenReturn(Optional.of(grupo));

        GrupoCompra resultado = grupoCompraService.entrarEmGrupo(1L, 10L);

        assertThat(resultado).isNotNull();
        verify(grupoCompraRepository).save(any(GrupoCompra.class));
        verify(participacaoRepository).save(any(Participacao.class));
    }

    @Test
    @DisplayName("entrarEmGrupo: deve reusar grupo ABERTO existente")
    void entrarEmGrupo_DeveReusarGrupoExistente() {
        when(grupoCompraRepository
                .findFirstByProdutoIdAndStatusGrupoOrderByIdAsc(1L, StatusGrupo.ABERTO))
                .thenReturn(Optional.of(grupo));
        when(grupoCompraRepository.findById(100L)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(participacaoRepository.existsByUsuarioIdAndGrupoCompraId(10L, 100L))
                .thenReturn(false);
        when(participacaoRepository.save(any(Participacao.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        grupoCompraService.entrarEmGrupo(1L, 10L);

        verify(grupoCompraRepository, never()).save(any(GrupoCompra.class));
        verify(participacaoRepository).save(any(Participacao.class));
    }

    @Test
    @DisplayName("entrarEmGrupo: deve incrementar contador do grupo")
    void entrarEmGrupo_DeveIncrementarContador() {
        int antes = grupo.getQtdParticipantes();

        when(grupoCompraRepository
                .findFirstByProdutoIdAndStatusGrupoOrderByIdAsc(1L, StatusGrupo.ABERTO))
                .thenReturn(Optional.of(grupo));
        when(grupoCompraRepository.findById(100L)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(participacaoRepository.existsByUsuarioIdAndGrupoCompraId(10L, 100L))
                .thenReturn(false);
        when(participacaoRepository.save(any(Participacao.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        grupoCompraService.entrarEmGrupo(1L, 10L);

        assertThat(grupo.getQtdParticipantes()).isEqualTo(antes + 1);
    }

    // ============================================================
    // ERROS
    // ============================================================

    @Test
    @DisplayName("adicionarParticipante: deve lançar exceção quando já participa")
    void adicionarParticipante_DeveLancarExcecao_QuandoJaParticipa() {
        when(grupoCompraRepository.findById(100L)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(participacaoRepository.existsByUsuarioIdAndGrupoCompraId(10L, 100L))
                .thenReturn(true);

        assertThatThrownBy(() -> grupoCompraService.adicionarParticipante(100L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já está participando");

        verify(participacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("adicionarParticipante: deve lançar exceção quando grupo não recebe mais")
    void adicionarParticipante_DeveLancarExcecao_QuandoGrupoFechado() {
        GrupoCompra grupoFechado = new GrupoCompra(produto, 10,
                new BigDecimal("425.00"), LocalDateTime.now().minusDays(1));
        setId(grupoFechado, 200L);

        when(grupoCompraRepository.findById(200L)).thenReturn(Optional.of(grupoFechado));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> grupoCompraService.adicionarParticipante(200L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não está mais recebendo");
    }

    @Test
    @DisplayName("adicionarParticipante: deve lançar exceção quando grupo não existe")
    void adicionarParticipante_DeveLancarExcecao_QuandoGrupoNaoExiste() {
        when(grupoCompraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grupoCompraService.adicionarParticipante(99L, 10L))
                .isInstanceOf(GrupoNaoEncontradoException.class);
    }

    // ============================================================
    // SAIR DO GRUPO
    // ============================================================

    @Test
    @DisplayName("sairDoGrupo: deve remover participação e decrementar contador")
    void sairDoGrupo_DeveRemoverParticipacao() {
        setQtdParticipantes(grupo, 3);

        when(grupoCompraRepository.findById(100L)).thenReturn(Optional.of(grupo));
        when(participacaoRepository.existsByUsuarioIdAndGrupoCompraId(10L, 100L))
                .thenReturn(true);

        grupoCompraService.sairDoGrupo(100L, 10L);

        assertThat(grupo.getQtdParticipantes()).isEqualTo(2);
        verify(participacaoRepository).deleteByUsuarioIdAndGrupoCompraId(10L, 100L);
    }

    @Test
    @DisplayName("sairDoGrupo: deve lançar exceção quando não participa")
    void sairDoGrupo_DeveLancarExcecao_QuandoNaoParticipa() {
        when(grupoCompraRepository.findById(100L)).thenReturn(Optional.of(grupo));
        when(participacaoRepository.existsByUsuarioIdAndGrupoCompraId(10L, 100L))
                .thenReturn(false);

        assertThatThrownBy(() -> grupoCompraService.sairDoGrupo(100L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não está participando");

        verify(participacaoRepository, never())
                .deleteByUsuarioIdAndGrupoCompraId(anyLong(), anyLong());
    }
    // ============================================================
    // Helpers para setar ID em entidades com @GeneratedValue
    // ============================================================
    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setQtdParticipantes(GrupoCompra g, int qtd) {
        try {
            var field = GrupoCompra.class.getDeclaredField("qtdParticipantes");
            field.setAccessible(true);
            field.set(g, qtd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setParticipacoes(GrupoCompra g, List<Participacao> list) {
        try {
            var field = GrupoCompra.class.getDeclaredField("participacoes");
            field.setAccessible(true);
            field.set(g, list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}