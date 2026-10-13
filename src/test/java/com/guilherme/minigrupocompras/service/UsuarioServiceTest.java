package com.guilherme.minigrupocompras.service;

import com.guilherme.minigrupocompras.dto.UsuarioFormDTO;
import com.guilherme.minigrupocompras.dto.UsuarioResponseDTO;
import com.guilherme.minigrupocompras.entity.Usuario;
import com.guilherme.minigrupocompras.exceptions.CpfJaCadastradoException;
import com.guilherme.minigrupocompras.exceptions.EmailJaCadastradoException;
import com.guilherme.minigrupocompras.exceptions.UsuarioNaoEncontradoException;
import com.guilherme.minigrupocompras.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    // ============================================================
    // CRIAR USUÁRIO
    // ============================================================

    @Test
    @DisplayName("criar: deve salvar usuário com senha criptografada")
    void criar_DeveSalvarUsuarioComSenhaCriptografada() {
        // Arrange
        UsuarioFormDTO form = new UsuarioFormDTO();
        form.setNome("Maria Silva");
        form.setCpf("123.456.789-01");
        form.setEmail("maria@teste.com");
        form.setSenha("senha123");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        // Act
        UsuarioResponseDTO resultado = usuarioService.criar(form);

        // Assert
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("Maria Silva");
        assertThat(resultado.email()).isEqualTo("maria@teste.com");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertThat(salvo.getSenha()).isEqualTo("$2a$hash");
        assertThat(salvo.getCpf()).isEqualTo("12345678901"); // CPF limpo
    }

    @Test
    @DisplayName("criar: deve lançar exceção quando email já existe")
    void criar_DeveLancarExcecao_QuandoEmailJaExiste() {
        UsuarioFormDTO form = new UsuarioFormDTO();
        form.setNome("Maria");
        form.setCpf("12345678901");
        form.setEmail("maria@teste.com");
        form.setSenha("senha123");

        when(usuarioRepository.existsByEmail("maria@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(form))
                .isInstanceOf(EmailJaCadastradoException.class)
                .hasMessageContaining("maria@teste.com");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: deve lançar exceção quando CPF já existe")
    void criar_DeveLancarExcecao_QuandoCpfJaExiste() {
        UsuarioFormDTO form = new UsuarioFormDTO();
        form.setNome("Maria");
        form.setCpf("12345678901");
        form.setEmail("maria@teste.com");
        form.setSenha("senha123");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(form))
                .isInstanceOf(CpfJaCadastradoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    // ============================================================
    // BUSCAR POR ID
    // ============================================================

    @Test
    @DisplayName("buscarPorId: deve retornar DTO quando usuário existe")
    void buscarPorId_DeveRetornarDTO_QuandoUsuarioExiste() {
        Usuario usuario = Usuario.builder()
                .id(1L).nome("Maria").cpf("12345678901")
                .email("maria@teste.com").senha("hash").ativo(true)
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponseDTO resultado = usuarioService.buscarPorId(1L);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("Maria");
        assertThat(resultado.email()).isEqualTo("maria@teste.com");
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando usuário não existe")
    void buscarPorId_DeveLancarExcecao_QuandoNaoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(UsuarioNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    // ============================================================
    // BUSCAR POR EMAIL (LOGIN)
    // ============================================================

    @Test
    @DisplayName("buscarPorEmailParaLogin: deve retornar entity quando existe")
    void buscarPorEmailParaLogin_DeveRetornarUsuario() {
        Usuario usuario = Usuario.builder()
                .id(1L).email("maria@teste.com").senha("hash").build();
        when(usuarioRepository.findByEmail("maria@teste.com"))
                .thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorEmailParaLogin("maria@teste.com");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo("maria@teste.com");
    }

    @Test
    @DisplayName("buscarPorEmailParaLogin: deve lançar exceção quando não existe")
    void buscarPorEmailParaLogin_DeveLancarExcecao() {
        when(usuarioRepository.findByEmail("x@y.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorEmailParaLogin("x@y.com"))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    // ============================================================
    // VALIDAÇÃO DE EMAIL (HTMX)
    // ============================================================

    @Test
    @DisplayName("emailJaExiste: deve retornar true quando existe")
    void emailJaExiste_DeveRetornarTrue() {
        when(usuarioRepository.existsByEmail("maria@teste.com")).thenReturn(true);
        assertThat(usuarioService.emailJaExiste("maria@teste.com")).isTrue();
    }

    @Test
    @DisplayName("emailJaExiste: deve retornar false quando não existe")
    void emailJaExiste_DeveRetornarFalse() {
        when(usuarioRepository.existsByEmail("novo@teste.com")).thenReturn(false);
        assertThat(usuarioService.emailJaExiste("novo@teste.com")).isFalse();
    }
}