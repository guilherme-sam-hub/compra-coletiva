package com.guilherme.minigrupocompras.controller;

import com.guilherme.minigrupocompras.dto.UsuarioFormDTO;
import com.guilherme.minigrupocompras.dto.UsuarioResponseDTO;
import com.guilherme.minigrupocompras.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ═══════════════════════════════════════════
    // GET: Mostra a página de cadastro
    // ═══════════════════════════════════════════
    @GetMapping("/cadastro")
    public String paginaCadastro(Model model) {
        // Cria um DTO vazio para o Thymeleaf preencher o formulário
        model.addAttribute("usuario", new UsuarioFormDTO());
        return "usuario/cadastro"; // templates/usuario/cadastro.html
    }

    // ═══════════════════════════════════════════
    // POST: Processa o cadastro (HTMX)
    // ═══════════════════════════════════════════
    @PostMapping("/cadastro")
    public String cadastrar(
            @Valid @ModelAttribute("usuario") UsuarioFormDTO usuario,
            BindingResult result,
            Model model) {

        log.info("Tentativa de cadastro para: {}", usuario.getEmail());

        // 1. Se houver erros de validação (Bean Validation), retorna o formulário com erros
        if (result.hasErrors()) {
            log.warn("Erros de validação: {}", result.getAllErrors());
            return "usuario/fragments :: formulario";
        }

        // 2. Tenta criar o usuário (pode lançar exceptions de negócio)
        try {
            UsuarioResponseDTO usuarioCriado = usuarioService.criar(usuario);
            log.info("Usuário criado com sucesso: {}", usuarioCriado.email());

            model.addAttribute(
                    "sucesso", "Conta criada com sucesso! Redirecionando para login..."
            );

            return "usuario/fragments :: mensagem-sucesso";

        } catch (Exception e) {
            // 3. Se houver erro de negócio (email/CPF duplicado), retorna mensagem de erro
            log.error("Erro ao cadastrar usuário: {}", e.getMessage());
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("usuario", usuario); // mantém os dados preenchidos
            return "usuario/fragments :: mensagem-erro";
        }
        }
    //validação em tempo real
    @PostMapping("/validar-email")
    public String validarEmail(@RequestParam String email, Model model) {
        if (usuarioService.emailJaExiste(email)) {
            model.addAttribute("erroCampo", "⚠️ Este email já está cadastrado");
        }
        // Se não houver erro, erroCampo será null e o fragmento renderiza vazio
        return "usuario/fragments :: erro-campo";
    }
}