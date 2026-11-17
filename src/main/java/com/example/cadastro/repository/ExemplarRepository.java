package com.example.cadastro.repository;

import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.SituacaoEmprestimo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExemplarRepository extends JpaRepository<Exemplar, Long> {

    boolean existsByCodigoPatrimonio(String codigoPatrimonio);

    @Query("""
    select exemplar
    from Exemplar exemplar
    where exemplar.livro.id = :livroId
      and exemplar.livro.ativo = true
      and exemplar.ativo = true
    """)
    List<Exemplar> findAtivosPorLivro(@Param("livroId") Long livroId);

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

    List<Exemplar> findByLivroIdAndAtivoFalse(Long livroId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Exemplar e where e.id = :id")
    Optional<Exemplar> buscarPorIdComBloqueio(@Param("id") Long id);
}
