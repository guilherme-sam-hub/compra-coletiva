package com.guilherme.minigrupocompras.entity;


import com.guilherme.minigrupocompras.enums.StatusGrupo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "grupo_compras")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class GrupoCompra {
    // COLUNAS DA TABELA
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meta_participantes", nullable = false)
    private Integer metaParticipantes;

    @Column(name = "qtd_participantes")
    private Integer qtdParticipantes;

    @Column(name = "preco_desconto", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoDesconto;

    @Enumerated(EnumType.STRING) //SALVA ABERTO, SUCESSO, etc.
    @Column(nullable = false)
    private StatusGrupo statusGrupo = StatusGrupo.ABERTO; //chamando enum

    @Column(name = "data_expiração")
    private LocalDateTime dataExpiracao;

    //RELAÇÕES - LADO DONO
    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    //RELAÇÕES LADO INVERSO
    //Apenas Mapeamento <mappedBy>.
    @OneToMany(mappedBy = "grupoCompra", fetch = FetchType.LAZY)
    private List<Participacao> participacoes;

    @OneToMany(mappedBy = "grupoCompra", fetch = FetchType.LAZY)
    private List<Pedido> pedidos;

    //CONSTRUTOR
    public GrupoCompra(
            Produto produto, Integer metaParticipantes, BigDecimal precoDesconto,
            LocalDateTime dataExpiracao
    ){
        // Atribuição de valores
        this.produto = produto;
        this.metaParticipantes = metaParticipantes;
        this.precoDesconto = precoDesconto;
        this.dataExpiracao = dataExpiracao;

        // Valores padrão (invariantes)
        this.qtdParticipantes = 0;
        this.statusGrupo = StatusGrupo.ABERTO;

    }
    //MÉTODOS DE DOMÍNIO
    public void adicionarParticipante() {
        if (this.statusGrupo != StatusGrupo.ABERTO) {
            throw new IllegalStateException("Este grupo não está mais recebendo pessoas.");
        }

        this.qtdParticipantes++;

        if (this.qtdParticipantes >= this.metaParticipantes) {
                this.statusGrupo = StatusGrupo.SUCESSO;
            }
    }
    public void removerParticipante() {
        if (this.qtdParticipantes != null && this.qtdParticipantes > 0) {
            this.qtdParticipantes--;
        }
        // Não mexe no status: se já virou SUCESSO, continua SUCESSO
    }
    public void marcarGrupoExpirado(){
        if (this.statusGrupo == StatusGrupo.ABERTO) {
            this.statusGrupo = StatusGrupo.EXPIRADO;
        }
    }
    public void cancelar(){
        if (this.statusGrupo == StatusGrupo.ABERTO) {
            this.statusGrupo = StatusGrupo.CANCELADO;
        }
    }
    public boolean podeReceberParticipantes(LocalDateTime dataAtual){
        return this.statusGrupo == StatusGrupo.ABERTO
                && this.dataExpiracao != null
                && this.dataExpiracao.isAfter(dataAtual);
    }
}
