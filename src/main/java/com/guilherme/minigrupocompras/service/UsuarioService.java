package com.guilherme.minigrupocompras.service;

import com.guilherme.minigrupocompras.dto.UsuarioAdminResponseDTO;
import com.guilherme.minigrupocompras.dto.UsuarioEditFormDTO;
import com.guilherme.minigrupocompras.dto.UsuarioFormDTO;
import com.guilherme.minigrupocompras.dto.UsuarioResponseDTO;
import com.guilherme.minigrupocompras.entity.Usuario;
import com.guilherme.minigrupocompras.enums.Role;
import com.guilherme.minigrupocompras.exceptions.CpfJaCadastradoException;
import com.guilherme.minigrupocompras.exceptions.EmailJaCadastradoException;
import com.guilherme.minigrupocompras.exceptions.UsuarioNaoEncontradoException;
import com.guilherme.minigrupocompras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // ============================================================
    // AUXILIAR
    // ============================================================
    private String limparCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    private boolean senhaPreenchida(String senha) {
        return senha != null && !senha.isBlank();
    }

    // ============================================================
    // CRIAR
    // ============================================================
    @Transactional(readOnly = true)
    public boolean emailJaExiste(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioFormDTO formDto) {
        log.info("Iniciando cadastro para o email: {}", formDto.getEmail());

        String cpfLimpo = limparCpf(formDto.getCpf());

        if (usuarioRepository.existsByEmail(formDto.getEmail())) {
            throw new EmailJaCadastradoException(formDto.getEmail());
        }
        if (usuarioRepository.existsByCpf(cpfLimpo)) {
            throw new CpfJaCadastradoException(cpfLimpo);
        }

        Usuario usuario = Usuario.builder()
                .nome(formDto.getNome())
                .cpf(cpfLimpo)
                .email(formDto.getEmail())
                .senha(passwordEncoder.encode(formDto.getSenha()))
                .ativo(true)
                .role(Role.USER)
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuário salvo com sucesso. ID: {}", salvo.getId());

        return new UsuarioResponseDTO(salvo.getId(), salvo.getNome(),
                salvo.getCpf(), salvo.getEmail());
    }

    // ============================================================
    // BUSCAR
    // ============================================================
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        return new UsuarioResponseDTO(u.getId(), u.getNome(), u.getCpf(), u.getEmail());
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmailParaLogin(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(email));
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getId))
                .map(u -> new UsuarioAdminResponseDTO(
                        u.getId(), u.getNome(), u.getCpf(), u.getEmail(),
                        u.getRole().name(), u.getAtivo(), u.getDataCadastro()))
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioEditFormDTO buscarFormEdicao(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        return new UsuarioEditFormDTO(u.getNome(), u.getEmail(), null);
    }

    @Transactional(readOnly = true)
    public UsuarioAdminResponseDTO buscarAdminDTO(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        return new UsuarioAdminResponseDTO(u.getId(), u.getNome(), u.getCpf(),
                u.getEmail(), u.getRole().name(), u.getAtivo(), u.getDataCadastro());
    }

    // ============================================================
    // ATUALIZAR (usado pelo admin e pelo próprio usuário)
    // ============================================================
    @Transactional
    public void atualizarPorId(Long id, UsuarioEditFormDTO form) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        aplicarEdicao(usuario, form);
    }

    @Transactional
    public void atualizarPorEmail(String emailAtual, UsuarioEditFormDTO form) {
        Usuario usuario = usuarioRepository.findByEmail(emailAtual)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(emailAtual));
        aplicarEdicao(usuario, form);
    }

    private void aplicarEdicao(Usuario usuario, UsuarioEditFormDTO form) {
        // Unicidade de email só se mudou
        if (!usuario.getEmail().equalsIgnoreCase(form.getEmail())
                && usuarioRepository.existsByEmail(form.getEmail())) {
            throw new EmailJaCadastradoException(form.getEmail());
        }

        usuario.setNome(form.getNome());
        usuario.setEmail(form.getEmail());

        if (senhaPreenchida(form.getSenha())) {
            usuario.setSenha(passwordEncoder.encode(form.getSenha()));
        }

        usuarioRepository.save(usuario);
        log.info("Usuário {} atualizado.", usuario.getEmail());
    }

    // ============================================================
    // DELETAR (admin)
    // ============================================================
    @Transactional
    public void deletarPeloAdmin(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        // Regra 2b: não pode deletar admin
        if (usuario.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Não é possível deletar um administrador.");
        }

        usuarioRepository.delete(usuario);
        log.info("Usuário ID {} deletado pelo admin.", id);
    }

    // ============================================================
    // DELETAR (própria conta)
    // ============================================================
    @Transactional
    public void deletarPropriaConta(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(email));

        // Segurança: usuário comum não pode deletar a própria conta se for ADMIN
        if (usuario.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Admins não podem deletar a própria conta por aqui.");
        }

        usuarioRepository.delete(usuario);
        log.info("Usuário {} deletou a própria conta.", email);
    }
}