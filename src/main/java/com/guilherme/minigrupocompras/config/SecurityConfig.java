package com.guilherme.minigrupocompras.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Liga o módulo de segurança do Spring
public class SecurityConfig {

    // padrão de criptografia BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cadeia de filtros: quem acessa o quê e como o login funciona
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Rotas públicas — qualquer um acessa, mesmo deslogado
                        .requestMatchers(
                                "/", "/login",
                                "/usuarios/cadastro", "/usuarios/validar-email",
                                "/esqueci-senha", "/reset-senha",
                                "/css/**", "/js/**", "/imagens/**"
                        ).permitAll()

                        // Área administrativa — só quem tem ROLE_ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Demais rotas — exige login (inclui /produtos, /compras etc.)
                        .anyRequest().authenticated()
                )

                // Configura o formulário de login
                .formLogin(form -> form
                        .loginPage("/login")              // Usa sua página customizada
                        .loginProcessingUrl("/login")     // URL que recebe email+senha (padrão)
                        .defaultSuccessUrl("/produtos", true) // Sempre vai para /produtos após login
                        .failureUrl("/login?error=true")  // Se errar a senha, volta com ?error
                        .permitAll()
                )

                // Configura o logout
                .logout(logout -> logout
                        .logoutUrl("/logout")             // URL que encerra a sessão
                        .logoutSuccessUrl("/login?logout=true") // Volta ao login com mensagem de sucesso
                        .invalidateHttpSession(true)      // Destrói a sessão no servidor
                        .deleteCookies("JSESSIONID")      // Limpa o cookie do navegador
                        .permitAll()
                );

        return http.build();
    }
}