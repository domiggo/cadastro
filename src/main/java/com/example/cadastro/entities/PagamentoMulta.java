package com.example.cadastro.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_pagamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoMulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    @DecimalMin(value = "0.01", message = "O pagamento deve ser de pelo menos R$0,01")
    private BigDecimal valor;

    @Column(nullable = false)
    @NotNull
    private LocalDate dataPagamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "emprestimo_id", nullable = false)
    private Emprestimo emprestimo;
}
