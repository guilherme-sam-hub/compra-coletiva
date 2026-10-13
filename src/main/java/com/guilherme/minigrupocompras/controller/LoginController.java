package com.guilherme.minigrupocompras.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String paginaLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        // Se veio com ?error=true, mostra mensagem de erro
        if (error != null) {
            model.addAttribute("erro", "Email ou senha inválidos");
        }

        // Se veio com ?logout=true, mostra mensagem de sucesso
        if (logout != null) {
            model.addAttribute("sucesso", "Você saiu com sucesso");
        }

        return "login"; // templates/login.html
    }
}