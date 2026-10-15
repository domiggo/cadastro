package com.example.cadastro.repository;

import com.example.cadastro.entities.LivrosModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LivrosRepository
        extends JpaRepository<LivrosModel, Long> {

    @Query("""
        select livro
        from LivrosModel livro
        where not exists (
            select emprestimo.id
            from Emprestimo emprestimo
            where emprestimo.livro = livro
              and emprestimo.dataDevolucao is null
        )
        """)
    List<LivrosModel> findDisponiveis();
}