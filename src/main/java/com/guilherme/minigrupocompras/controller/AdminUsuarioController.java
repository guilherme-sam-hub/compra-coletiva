package com.guilherme.minigrupocompras.controller;

import com.guilherme.minigrupocompras.dto.UsuarioEditFormDTO;
import com.guilherme.minigrupocompras.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
@Slf4j
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "admin/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String formEdicao(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.buscarFormEdicao(id));
        model.addAttribute("usuarioInfo", usuarioService.buscarAdminDTO(id));
        model.addAttribute("modo", "admin");
        return "admin/usuario-form";
    }

    @PostMapping("/{id}/editar")
    public String salvar(@PathVariable Long id,
                         @Valid @ModelAttribute("usuario") UsuarioEditFormDTO form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {

        if (result.hasErrors()) {
            model.addAttribute("usuarioInfo", usuarioService.buscarAdminDTO(id));
            model.addAttribute("modo", "admin");
            return "admin/usuario-form";
        }

        try {
            usuarioService.atualizarPorId(id, form);
            ra.addFlashAttribute("sucesso", "Usuário atualizado com sucesso.");
        } catch (Exception e) {
            log.warn("Falha ao atualizar usuário {}: {}", id, e.getMessage());
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            usuarioService.deletarPeloAdmin(id);
            ra.addFlashAttribute("sucesso", "Usuário deletado.");
        } catch (Exception e) {
            log.warn("Falha ao deletar usuário {}: {}", id, e.getMessage());
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}