package com.guilherme.minigrupocompras.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "participacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Participacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_entrada")
    private LocalDateTime dataEntrada;

    @PrePersist
    public void prePersist() {
        this.dataEntrada = LocalDateTime.now();
    }

    //RELAÇÕES "DONO" da relação é sempre @ManyToOne (filho).
    // Usa @JoinColumn.
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "grupo_compra_id")
    private GrupoCompra grupoCompra;


}
