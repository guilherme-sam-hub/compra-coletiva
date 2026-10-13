package com.guilherme.minigrupocompras.exceptions;

import com.guilherme.minigrupocompras.dto.UsuarioFormDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice  // Intercepta exceptions de TODOS os controllers
@Slf4j
public class GlobalExceptionHandler {


    // ERROS DE NEGÓCIO (exceptions customizadas)


    @ExceptionHandler(EmailJaCadastradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleEmailDuplicado(EmailJaCadastradoException ex, Model model) {
        log.warn("Email duplicado: {}", ex.getMessage());

        model.addAttribute("erro", ex.getMessage());
        model.addAttribute("usuario", new UsuarioFormDTO());

        return "usuario/fragments :: mensagem-erro";  //  retorna fragmento HTMX
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex, Model model) {
        log.warn("Usuário não encontrado: {}", ex.getMessage());

        model.addAttribute("erro", "Usuário não encontrado.");
        return "erro/nao-encontrado";  // página 404 customizada
    }

    @ExceptionHandler(GrupoExpiradoException.class)
    @ResponseStatus(HttpStatus.GONE)
    public String handleGrupoExpirado(GrupoExpiradoException ex, Model model) {
        log.warn("⚠️ Grupo expirado: {}", ex.getMessage());

        model.addAttribute("erro", "Este grupo de compra já expirou.");
        return "erro/grupo-expirado";
    }

    @ExceptionHandler(GrupoLotadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleGrupoLotado(GrupoLotadoException ex, Model model) {
        log.warn("Grupo lotado: {}", ex.getMessage());

        model.addAttribute("erro", ex.getMessage());
        return "erro/grupo-lotado";
    }

    // ═══════════════════════════════════════════
    //  ERROS DE VALIDAÇÃO (Bean Validation)
    // ═══════════════════════════════════════════

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidacao(MethodArgumentNotValidException ex, Model model) {
        log.warn("Erro de validação: {}", ex.getMessage());

        // Coleta todos os erros de campo
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensagem = error.getDefaultMessage();
            erros.put(campo, mensagem);
        });

        model.addAttribute("erros", erros);
        model.addAttribute("usuario", new com.guilherme.minigrupocompras.dto.UsuarioFormDTO());

        return "usuario/fragments :: formulario";  // 🎯 retorna form com erros inline
    }

    // ═══════════════════════════════════════════
    // 3️⃣ ERROS GENÉRICOS (catch-all)
    // ═══════════════════════════════════════════

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenerico(Exception ex, Model model) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);

        model.addAttribute("erro", "Ocorreu um erro inesperado. Tente novamente.");
        return "erro/generico";
    }
}