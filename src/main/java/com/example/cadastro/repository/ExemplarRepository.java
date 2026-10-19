package com.example.cadastro.repository;

import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.SituacaoEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExemplarRepository extends JpaRepository<Exemplar, Long> {

    boolean existsByCodigoPatrimonio(String codigoPatrimonio);

    List<Exemplar> findByLivroIdAndAtivoTrue(Long livroId);

    @Modifying
    @Query("delete from Exemplar e where e.livro.id = :livroId")
    void excluirPorLivroId(@Param("livroId") Long livroId);

    @Query("""
    select exemplar
    from Exemplar exemplar
    where exemplar.livro.id = :livroId
      and exemplar.livro.ativo = true
      and exemplar.ativo = true
      and not exists (
          select emprestimo.id
          from Emprestimo emprestimo
          where emprestimo.exemplar = exemplar
            and emprestimo.situacao = :situacao
      )
    """)
    List<Exemplar> findDisponiveisPorLivro(
            @Param("livroId") Long livroId,
            @Param("situacao") SituacaoEmprestimo situacao
    );
}
