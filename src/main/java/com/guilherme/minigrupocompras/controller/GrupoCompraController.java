package com.guilherme.minigrupocompras.controller;

import com.guilherme.minigrupocompras.dto.GrupoCompraResponseDTO;
import com.guilherme.minigrupocompras.entity.Usuario;
import com.guilherme.minigrupocompras.service.GrupoCompraService;
import com.guilherme.minigrupocompras.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/grupos")
@RequiredArgsConstructor
@Slf4j
public class GrupoCompraController {

    private final GrupoCompraService grupoCompraService;
    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id,
                           Authentication auth,
                           Model model) {
        GrupoCompraResponseDTO grupo = grupoCompraService.buscarDetalhes(id, auth.getName());
        model.addAttribute("grupo", grupo);
        return "grupo/detalhes";
    }

    @PostMapping("/{id}/entrar")
    public String entrar(@PathVariable Long id,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            Usuario usuario = usuarioService.buscarPorEmailParaLogin(auth.getName());
            grupoCompraService.adicionarParticipante(id, usuario.getId());
            ra.addFlashAttribute("sucesso", "Você entrou no grupo!");
        } catch (Exception e) {
            log.warn("Falha ao entrar no grupo {}: {}", id, e.getMessage());
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/grupos/" + id;
    }

    @PostMapping("/{id}/sair")
    public String sair(@PathVariable Long id,
                       Authentication auth,
                       RedirectAttributes ra) {
        try {
            Usuario usuario = usuarioService.buscarPorEmailParaLogin(auth.getName());
            grupoCompraService.sairDoGrupo(id, usuario.getId());
            ra.addFlashAttribute("sucesso", "Você saiu do grupo.");
        } catch (Exception e) {
            log.warn("Falha ao sair do grupo {}: {}", id, e.getMessage());
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/grupos/" + id;
    }
}