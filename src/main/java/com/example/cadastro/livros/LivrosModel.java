package com.example.cadastro.livros;

import com.example.cadastro.model.Usuario;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_livros")
public class LivrosModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private int diasAlugado;

    @ManyToOne
    @JoinColumn(name = "livros_id") //foreing key
    private Usuario usuario;

    public LivrosModel() {
    }

    public LivrosModel(Long id, String nome, int diasAlugado) {
        this.id = id;
        this.nome = nome;
        this.diasAlugado = diasAlugado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getDiasAlugado() {
        return diasAlugado;
    }

    public void setDiasAlugado(int diasAlugado) {
        this.diasAlugado = diasAlugado;
    }
}
