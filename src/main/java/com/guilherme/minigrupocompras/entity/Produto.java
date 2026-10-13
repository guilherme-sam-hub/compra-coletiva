package com.guilherme.minigrupocompras.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.AccessLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Necessário para o JPA
@EqualsAndHashCode(of = "id")
public class Produto {

    // --- CAMPOS ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "preco_original", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoOriginal;

    @Column(name = "imagem_url")
    private String imagem;

    @Column(nullable = false)
    private Integer quantidadeMinima = 1;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @OneToMany(mappedBy = "produto", fetch = FetchType.LAZY)
    private List<GrupoCompra> grupoCompras;

    // --- CONSTRUTOR COM BUILDER ---
    @Builder
    public Produto(String nome, String descricao, BigDecimal precoOriginal, String imagem,
                   Integer quantidadeMinima, Boolean ativo, LocalDateTime dataCadastro) {
        this.nome = nome;
        this.descricao = descricao;
        this.precoOriginal = precoOriginal;
        this.imagem = imagem;
        // fallback (protege contra nulos vindos do Builder)
        this.quantidadeMinima = quantidadeMinima != null ? quantidadeMinima : 1;
        this.ativo = ativo != null ? ativo : true;
        this.dataCadastro = dataCadastro != null ? dataCadastro : LocalDateTime.now();
    }
}