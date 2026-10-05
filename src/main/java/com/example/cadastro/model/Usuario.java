package com.example.cadastro.model;

import com.example.cadastro.livros.LivrosModel;
import jakarta.persistence.*;

import java.util.List;

@Entity // transforma a classe em entidade no banco de dados
@Table(name = "tb_cadastro")
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private int cpf;

    @OneToMany(mappedBy = "usuario")
    private List<LivrosModel> livrosModels;

    public Usuario() {
    }

    public Usuario(String nome, String email, int cpf) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCpf() {
        return cpf;
    }

    public void setCpf(int cpf) {
        this.cpf = cpf;
    }
}
