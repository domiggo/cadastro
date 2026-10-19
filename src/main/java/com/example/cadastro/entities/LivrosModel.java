package com.example.cadastro.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_livros")
@NoArgsConstructor //construtor vazio
@AllArgsConstructor //construtor com todos os atributos
@Data //gets e sets
public class LivrosModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do livro é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;
}