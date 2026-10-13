package com.guilherme.minigrupocompras.controller;

import com.guilherme.minigrupocompras.dto.ProdutoFormDTO;
import com.guilherme.minigrupocompras.dto.ProdutoResponseDTO;
import com.guilherme.minigrupocompras.entity.GrupoCompra;
import com.guilherme.minigrupocompras.entity.Produto;
import com.guilherme.minigrupocompras.entity.Usuario;
import com.guilherme.minigrupocompras.mapper.ProdutoMapper;
import com.guilherme.minigrupocompras.repository.ProdutoRepository;
import com.guilherme.minigrupocompras.service.GrupoCompraService;
import com.guilherme.minigrupocompras.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Slf4j
public class ProdutoController {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;
    private final GrupoCompraService grupoCompraService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listarProdutos(Model model) {
        List<ProdutoResponseDTO> produtos = produtoMapper.toResponseDTOList(
                produtoRepository.findAll()
        );
        model.addAttribute("produtos", produtos);
        return "produtos";
    }

    @GetMapping("/cadastro")
    public String paginaCadastro(Model model) {
        model.addAttribute("produto", new ProdutoFormDTO());
        return "produto/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @Valid @ModelAttribute("produto") ProdutoFormDTO produtoForm,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "produto/fragments :: formulario";
        }

        try {
            Produto produto = Produto.builder()
                    .nome(produtoForm.getNome())
                    .descricao(produtoForm.getDescricao())
                    .precoOriginal(produtoForm.getPreco())
                    .imagem(produtoForm.getImagem())
                    .quantidadeMinima(produtoForm.getQuantidadeMinima())
                    .ativo(true)
                    .build();

            Produto salvo = produtoRepository.save(produto);
            log.info("Produto salvo com sucesso. ID: {}", salvo.getId());

            model.addAttribute("sucesso", "Produto '" + salvo.getNome() + "' cadastrado com sucesso!");
            return "produto/fragments :: mensagem-sucesso";

        } catch (Exception e) {
            log.error("Erro ao cadastrar produto: {}", e.getMessage());
            model.addAttribute("erro", "Erro ao salvar produto: " + e.getMessage());
            model.addAttribute("produto", produtoForm);
            return "produto/fragments :: mensagem-erro";
        }
    }

    @PostMapping("/{id}/participar")
    public String participar(@PathVariable Long id,
                             Authentication auth,
                             RedirectAttributes ra) {
        try {
            Usuario usuario = usuarioService.buscarPorEmailParaLogin(auth.getName());
            GrupoCompra grupo = grupoCompraService.entrarEmGrupo(id, usuario.getId());
            return "redirect:/grupos/" + grupo.getId();
        } catch (Exception e) {
            log.warn("Falha ao participar do produto {}: {}", id, e.getMessage());
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/produtos";
        }
    }
}