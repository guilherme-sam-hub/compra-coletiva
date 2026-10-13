package com.guilherme.minigrupocompras.config;

import com.guilherme.minigrupocompras.entity.Produto;
import com.guilherme.minigrupocompras.enums.Role;
import com.guilherme.minigrupocompras.entity.Usuario;
import com.guilherme.minigrupocompras.repository.ProdutoRepository;
import com.guilherme.minigrupocompras.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        popularProdutos();
        criarAdminPadrao();
    }

    // ============================================================
    // 1) Produtos
    // ============================================================
    private void popularProdutos() {
        if (produtoRepository.count() > 0) {
            log.info("⏭️ Banco já possui produtos. Pulando o DataLoader de Produtos.");
            return;
        }

        log.info("📦 Populando banco de dados com produtos iniciais...");

        Produto p1 = Produto.builder()
                .nome("SSD NVMe Kingston 1TB")
                .descricao("SSD NVMe PCIe 4.0, leitura até 7000 MB/s. Ideal para games e edição.")
                .precoOriginal(new BigDecimal("549.90"))
                .imagem("https://thumbs.dreamstime.com/b/galati-romania-december-presenting-new-kingston-kc-pcie-nvme-m-ssd-to-market-266587856.jpg")
                .quantidadeMinima(10)
                .build();

        Produto p2 = Produto.builder()
                .nome("Teclado Mecânico Redragon Kumara")
                .descricao("Switch Outemu Red, ABNT2, iluminação RGB, anti-ghosting.")
                .precoOriginal(new BigDecimal("199.90"))
                .imagem("https://thumbs.dreamstime.com/b/modern-wireless-mechanical-gaming-keyboard-isolated-white-background-sleek-black-keypad-sits-angled-against-bright-478706345.jpg")
                .quantidadeMinima(15)
                .build();

        Produto p3 = Produto.builder()
                .nome("Mouse Gamer Logitech G502 Hero")
                .descricao("Sensor HERO 25K, 11 botões programáveis, RGB LIGHTSYNC.")
                .precoOriginal(new BigDecimal("329.90"))
                .imagem("https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=500")
                .quantidadeMinima(8)
                .build();

        Produto p4 = Produto.builder()
                .nome("Headset HyperX Cloud II")
                .descricao("Som surround 7.1 virtual, espuma memory foam, microfone destacável.")
                .precoOriginal(new BigDecimal("449.90"))
                .imagem("https://images.unsplash.com/photo-1599669454699-248893623440?auto=format&fit=crop&w=500")
                .quantidadeMinima(12)
                .build();

        Produto p5 = Produto.builder()
                .nome("Monitor Gamer LG UltraGear 24\" 144Hz")
                .descricao("Painel IPS Full HD, 1ms, FreeSync Premium, bordas ultrafinas.")
                .precoOriginal(new BigDecimal("899.00"))
                .imagem("https://thumbs.dreamstime.com/b/immersive-gaming-setup-monitor-keyboard-controller-neon-lit-room-high-tech-featuring-vibrant-showing-shooting-game-318783011.jpg")
                .quantidadeMinima(6)
                .build();

        Produto p6 = Produto.builder()
                .nome("Webcam Logitech C920 Full HD")
                .descricao("1080p a 30fps, foco automático, microfone estéreo duplo, USB plug-and-play.")
                .precoOriginal(new BigDecimal("379.90"))
                .imagem("https://img.magnific.com/premium-psd/logitech-c920-hd-pro-webcam-transparent-background_679658-32487.jpg?semt=ais_hybrid&w=740&q=80")
                .quantidadeMinima(10)
                .build();
        produtoRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6));
        log.info("✅ {} produtos cadastrados com sucesso!", produtoRepository.count());
    }
        // ============================================================
        // 2) Usuário ADMIN padrão
        // ============================================================
        private void criarAdminPadrao() {
            // Idempotência: só cria se ainda não existir nenhum admin
            if (usuarioRepository.existsByEmail("admin@minigrupo.com")) {
                log.info("⏭️ Usuário admin já existe. Pulando o DataLoader de Admin.");
                return;
            }

            log.info("👤 Criando usuário ADMIN padrão...");

            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .cpf("00000000000")
                    .email("admin@minigrupo.com")
                    .senha(passwordEncoder.encode("123456"))             // senha criptografada
                    .ativo(true)
                    .role(Role.ADMIN)
                    .build();

            usuarioRepository.save(admin);
            log.info("✅ Usuário admin criado! Email: admin@minigrupo.com | Senha: 123456");
        }
    }