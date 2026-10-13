package com.guilherme.minigrupocompras.service;

import com.guilherme.minigrupocompras.entity.PasswordResetToken;
import com.guilherme.minigrupocompras.entity.Usuario;
import com.guilherme.minigrupocompras.repository.PasswordResetTokenRepository;
import com.guilherme.minigrupocompras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.reset-password.dev-mode:false}")
    private boolean devMode;

    @Value("${app.reset-password.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Sempre chamado com sucesso, mesmo se o email não existir.
     * Evita user enumeration: atacante não descobre quem tem conta.
     */
    @Transactional
    public void solicitarReset(String email) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            // 1) Invalida tokens anteriores
            tokenRepository.deleteByUsuarioId(usuario.getId());

            // 2) Gera novo token
            String token = UUID.randomUUID().toString();
            PasswordResetToken prt = PasswordResetToken.builder()
                    .token(token)
                    .usuario(usuario)
                    .dataExpiracao(LocalDateTime.now().plusMinutes(30))
                    .usado(false)
                    .build();
            tokenRepository.save(prt);

            String link = baseUrl + "/reset-senha?token=" + token;

            // 3) DEV MODE: loga o link no console
            if (devMode) {
                log.info("🔑 [DEV MODE] Link de reset para {}: {}", email, link);
            }

            // 4) Envia email (falha não derruba a requisição)
            try {
                emailService.enviarLinkResetSenha(email, usuario.getNome(), link);
            } catch (Exception e) {
                log.warn("Email não pôde ser enviado, mas o token foi criado: {}", e.getMessage());
            }
        });
    }

    @Transactional(readOnly = true)
    public boolean validarToken(String token) {
        return tokenRepository.findByTokenAndUsadoFalse(token)
                .filter(t -> !t.expirado())
                .isPresent();
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        PasswordResetToken prt = tokenRepository.findByTokenAndUsadoFalse(token)
                .filter(t -> !t.expirado())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado."));

        Usuario usuario = prt.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        prt.setUsado(true);
        tokenRepository.save(prt);

        log.info("Senha redefinida com sucesso para {}", usuario.getEmail());
    }
}