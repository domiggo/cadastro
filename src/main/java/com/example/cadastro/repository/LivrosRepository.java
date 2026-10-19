package com.example.cadastro.repository;

import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.SituacaoEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LivrosRepository
        extends JpaRepository<LivrosModel, Long> {

    @Query("""
    select livro
    from LivrosModel livro
    where livro.ativo = true
      and exists (
          select exemplar.id
          from Exemplar exemplar
          where exemplar.livro = livro
            and exemplar.ativo = true
            and not exists (
                select emprestimo.id
                from Emprestimo emprestimo
                where emprestimo.exemplar = exemplar
                  and emprestimo.situacao = :situacao
            )
      )
    """)
    List<LivrosModel> findDisponiveis(
            @Param("situacao")SituacaoEmprestimo situacao
            );
}