package com.example.cadastro.model;

import com.example.cadastro.livros.LivrosModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity // transforma a classe em entidade no banco de dados
@Table(name = "tb_cadastro")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String cpf;

    //@OneToMany  v- um usuario pode ter varios livros alugados
    @OneToMany(mappedBy = "usuario")
    private List<LivrosModel> livrosModels;
}
