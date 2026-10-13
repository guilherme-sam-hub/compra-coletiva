package com.guilherme.minigrupocompras.security;
import com.guilherme.minigrupocompras.entity.Usuario;
import com.guilherme.minigrupocompras.exceptions.UsuarioNaoEncontradoException;
import com.guilherme.minigrupocompras.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final  UsuarioService usuarioService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Tenta buscar o usuário no banco usando o serviço que criamos
        Usuario usuario;
        try {
            usuario = usuarioService.buscarPorEmailParaLogin(email);
        } catch (UsuarioNaoEncontradoException e) {
            // 2. Se não existir, lança a exceção padrão do Spring Security
            // Isso faz o login falhar com "Bad Credentials" sem expor que o email não existe
            throw new UsernameNotFoundException("Usuário não encontrado com o email: " + email, e);
        }

        // 3. Converte a Entity (Usuario) em um objeto que o Spring Security entende (UserDetails)
        return User.builder()
                .username(usuario.getEmail()) // O "login" será o email
                .password(usuario.getSenha()) // A senha JÁ CRIPTOGRAFADA do banco
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name())))  //Permissões
                .build();
    }
}
