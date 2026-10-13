package com.guilherme.minigrupocompras.controller;

import com.guilherme.minigrupocompras.dto.UsuarioEditFormDTO;
import com.guilherme.minigrupocompras.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
@RequiredArgsConstructor
@Slf4j
public class PerfilController {

    private final UsuarioService usuarioService;

    // ==============================================
    // 1) Ver o próprio perfil
    // ==============================================
    @GetMapping
    public String verPerfil(Authentication auth, Model model) {
        var usuario = usuarioService.buscarPorEmailParaLogin(auth.getName());
        model.addAttribute("usuario", usuario);
        return "perfil/detalhes";
    }

    // ==============================================
    // 2) Editar própria conta
    // ==============================================
    @GetMapping("/editar")
    public String formEdicao(Authentication auth, Model model) {
        var usuario = usuarioService.buscarPorEmailParaLogin(auth.getName());
        UsuarioEditFormDTO form = new UsuarioEditFormDTO(
                usuario.getNome(), usuario.getEmail(), null);
        model.addAttribute("usuario", form);
        model.addAttribute("usuarioInfo", usuario);
        model.addAttribute("modo", "perfil");
        return "perfil/form";
    }

    @PostMapping("/editar")
    public String salvar(Authentication auth,
                         @Valid @ModelAttribute("usuario") UsuarioEditFormDTO form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {

        if (result.hasErrors()) {
            var info = usuarioService.buscarPorEmailParaLogin(auth.getName());
            model.addAttribute("usuarioInfo", info);
            model.addAttribute("modo", "perfil");
            return "perfil/form";
        }

        try {
            usuarioService.atualizarPorEmail(auth.getName(), form);
            ra.addFlashAttribute("sucesso", "Dados atualizados com sucesso.");
            return "redirect:/perfil";
        } catch (Exception e) {
            log.warn("Falha ao atualizar perfil: {}", e.getMessage());
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/perfil/editar";
        }
    }

    // ==============================================
    // 3) Deletar própria conta
    // ==============================================
    @PostMapping("/deletar")
    public String deletar(Authentication auth,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          RedirectAttributes ra) {
        try {
            usuarioService.deletarPropriaConta(auth.getName());

            // Encerra a sessão após deletar
            new SecurityContextLogoutHandler().logout(request, response, auth);

            return "redirect:/login?contaRemovida=true";
        } catch (Exception e) {
            log.warn("Falha ao deletar própria conta: {}", e.getMessage());
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/perfil";
        }
    }
}