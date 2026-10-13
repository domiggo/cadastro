package com.example.cadastro.livros;

import com.example.cadastro.model.Usuario;
import jakarta.persistence.*;
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
    private String nome;
    private int diasAlugado;

    //@ManyToOne - um livro tem um unico dono temporário
    @ManyToOne
    @JoinColumn(name = "livros_id") //foreing key
    private Usuario usuario;
}