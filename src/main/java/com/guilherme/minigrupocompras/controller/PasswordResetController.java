package com.guilherme.minigrupocompras.controller;

import com.guilherme.minigrupocompras.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService resetService;

    // ================================================
    // 1) Página "Esqueci minha senha"
    // ================================================
    @GetMapping("/esqueci-senha")
    public String paginaEsqueciSenha(Model model) {
        if (!model.containsAttribute("email")) {
            model.addAttribute("email", "");
        }
        return "esqueci-senha";
    }

    @PostMapping("/esqueci-senha")
    public String solicitarReset(@RequestParam String email, RedirectAttributes ra) {
        resetService.solicitarReset(email);
        // Sempre sucesso, mesmo se email não existe
        ra.addFlashAttribute("sucesso",
                "Se o email estiver cadastrado, enviaremos instruções para redefinir a senha.");
        return "redirect:/esqueci-senha";
    }

    // ================================================
    // 2) Página "Nova senha" (com token da URL)
    // ================================================
    @GetMapping("/reset-senha")
    public String paginaResetSenha(@RequestParam(required = false) String token,
                                   Model model,
                                   RedirectAttributes ra) {
        if (token == null || !resetService.validarToken(token)) {
            ra.addFlashAttribute("erro", "Link inválido ou expirado. Solicite novamente.");
            return "redirect:/esqueci-senha";
        }
        model.addAttribute("token", token);
        return "reset-senha";
    }

    @PostMapping("/reset-senha")
    public String redefinirSenha(@RequestParam String token,
                                 @RequestParam String senha,
                                 @RequestParam String senhaConfirmacao,
                                 RedirectAttributes ra) {

        if (!senha.equals(senhaConfirmacao)) {
            ra.addFlashAttribute("erro", "As senhas não coincidem.");
            return "redirect:/reset-senha?token=" + token;
        }
        if (senha.length() < 6) {
            ra.addFlashAttribute("erro", "A senha deve ter pelo menos 6 caracteres.");
            return "redirect:/reset-senha?token=" + token;
        }

        try {
            resetService.redefinirSenha(token, senha);
            ra.addFlashAttribute("sucesso", "Senha alterada com sucesso! Faça login.");
            return "redirect:/login";
        } catch (Exception e) {
            log.warn("Falha ao redefinir senha: {}", e.getMessage());
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/reset-senha?token=" + token;
        }
    }
}