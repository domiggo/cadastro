package com.example.cadastro.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_exemplares")
@Getter
@Setter
@NoArgsConstructor
public class Exemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private LivrosModel livro;

    @Column(nullable = false, unique = true)
    private String codigoPatrimonio;

    @Column(nullable = false)
    private boolean ativo = true;
}
