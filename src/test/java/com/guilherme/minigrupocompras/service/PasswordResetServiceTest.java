package com.guilherme.minigrupocompras.service;

import com.guilherme.minigrupocompras.entity.PasswordResetToken;
import com.guilherme.minigrupocompras.entity.Usuario;
import com.guilherme.minigrupocompras.repository.PasswordResetTokenRepository;
import com.guilherme.minigrupocompras.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .nome("Maria").cpf("12345678901")
                .email("maria@teste.com").senha("hash").ativo(true)
                .build();
        ReflectionTestUtils.setField(usuario, "id", 10L);

        // Injeta os @Value que não vêm do Mockito
        ReflectionTestUtils.setField(passwordResetService, "devMode", true);
        ReflectionTestUtils.setField(passwordResetService, "baseUrl", "http://localhost:8080");
    }

    // ============================================================
    // SOLICITAR RESET
    // ============================================================

    @Test
    @DisplayName("solicitarReset: deve gerar token e enviar email quando email existe")
    void solicitarReset_DeveGerarTokenEEnviarEmail() {
        when(usuarioRepository.findByEmail("maria@teste.com"))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.solicitarReset("maria@teste.com");

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());

        PasswordResetToken tokenSalvo = captor.getValue();
        assertThat(tokenSalvo.getToken()).isNotBlank();
        assertThat(tokenSalvo.getUsuario()).isEqualTo(usuario);
        assertThat(tokenSalvo.getUsado()).isFalse();
        assertThat(tokenSalvo.getDataExpiracao()).isAfter(LocalDateTime.now());

        verify(emailService).enviarLinkResetSenha(
                eq("maria@teste.com"), eq("Maria"), anyString());
    }

    @Test
    @DisplayName("solicitarReset: NÃO deve fazer nada quando email não existe (anti-enumeration)")
    void solicitarReset_NaoDeveFazerNada_QuandoEmailNaoExiste() {
        when(usuarioRepository.findByEmail("naoexiste@x.com"))
                .thenReturn(Optional.empty());

        passwordResetService.solicitarReset("naoexiste@x.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never())
                .enviarLinkResetSenha(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("solicitarReset: deve invalidar tokens anteriores do mesmo usuário")
    void solicitarReset_DeveInvalidarTokensAnteriores() {
        when(usuarioRepository.findByEmail("maria@teste.com"))
                .thenReturn(Optional.of(usuario));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.solicitarReset("maria@teste.com");

        verify(tokenRepository).deleteByUsuarioId(10L);
    }

    // ============================================================
    // VALIDAR TOKEN
    // ============================================================

    @Test
    @DisplayName("validarToken: deve retornar true quando token válido e não expirado")
    void validarToken_DeveRetornarTrue() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("abc").usuario(usuario).usado(false)
                .dataExpiracao(LocalDateTime.now().plusMinutes(10))
                .build();
        when(tokenRepository.findByTokenAndUsadoFalse("abc"))
                .thenReturn(Optional.of(token));

        assertThat(passwordResetService.validarToken("abc")).isTrue();
    }

    @Test
    @DisplayName("validarToken: deve retornar false quando token expirado")
    void validarToken_DeveRetornarFalse_QuandoExpirado() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("abc").usuario(usuario).usado(false)
                .dataExpiracao(LocalDateTime.now().minusMinutes(1))
                .build();
        when(tokenRepository.findByTokenAndUsadoFalse("abc"))
                .thenReturn(Optional.of(token));

        assertThat(passwordResetService.validarToken("abc")).isFalse();
    }

    @Test
    @DisplayName("validarToken: deve retornar false quando token não existe")
    void validarToken_DeveRetornarFalse_QuandoNaoExiste() {
        when(tokenRepository.findByTokenAndUsadoFalse("xxx")).thenReturn(Optional.empty());
        assertThat(passwordResetService.validarToken("xxx")).isFalse();
    }

    // ============================================================
    // REDEFINIR SENHA
    // ============================================================

    @Test
    @DisplayName("redefinirSenha: deve alterar senha e marcar token como usado")
    void redefinirSenha_DeveAlterarSenhaEMarcarToken() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("abc").usuario(usuario).usado(false)
                .dataExpiracao(LocalDateTime.now().plusMinutes(10))
                .build();
        when(tokenRepository.findByTokenAndUsadoFalse("abc"))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$novo");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.redefinirSenha("abc", "novaSenha123");

        assertThat(usuario.getSenha()).isEqualTo("$2a$novo");
        assertThat(token.getUsado()).isTrue();
        verify(usuarioRepository).save(usuario);
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("redefinirSenha: deve lançar exceção quando token inválido ou expirado")
    void redefinirSenha_DeveLancarExcecao_QuandoTokenInvalido() {
        when(tokenRepository.findByTokenAndUsadoFalse("invalido"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.redefinirSenha("invalido", "senha123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token inválido");

        verify(usuarioRepository, never()).save(any());
    }
}