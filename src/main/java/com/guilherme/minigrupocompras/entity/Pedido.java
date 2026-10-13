package com.guilherme.minigrupocompras.entity;

import com.guilherme.minigrupocompras.enums.StatusPagamento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // Salva "PENDENTE", "PAGO", etc.
    @Column(nullable = false)
    private StatusPagamento statusPagamento; //chamando enum

    @Column(name = "valor_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "data_Pedido")
    private LocalDateTime dataPedido;

    @PrePersist
    public void prePersist() {
        this.dataPedido= LocalDateTime.now();
    }

    //RELAÇÕES
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "grupo_compra_id")
    private GrupoCompra grupoCompra;

}
