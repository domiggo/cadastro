package com.example.cadastro.repository;


import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.SituacaoEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    Optional<Emprestimo> findByExemplarIdAndSituacao(Long exemplarId, SituacaoEmprestimo situacao);

    List<Emprestimo> findBySituacao(SituacaoEmprestimo situacao);

    List<Emprestimo> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndSituacao(Long usuarioId, SituacaoEmprestimo situacao);

    boolean existsByExemplarIdAndSituacao(Long exemplarId, SituacaoEmprestimo situacao);

    @Query("""

            select case when count(e) > 0 then true else false end
        from Emprestimo e
        where e.exemplar.livro.id = :livroId
        """)
    boolean existsByLivroId(@Param("livroId") Long livroId);

    @Modifying
    @Query("""
        delete from Emprestimo e
        where e.exemplar.livro.id = :livroId
        """)
    void excluirHistoricoDoLivro(@Param("livroId") Long livroId);

    @Modifying
    @Query("delete from Emprestimo e where e.usuario.id = :usuarioId")
    void excluirHistoricoDoUsuario(@Param("usuarioId") Long usuarioId);

    @Query("""
    select case when count(e) > 0 then true else false end
    from Emprestimo e
    where e.exemplar.livro.id = :livroId
      and e.situacao = :situacao
    """)
    boolean existeEmprestimoAtivoPorLivroESituacao(
            @Param("livroId") Long livroId,
            @Param("situacao") SituacaoEmprestimo situacao);

    @Query("""
    select e
    from Emprestimo e
    where e.situacao = :situacao
      and e.dataPrevistaDevolucao < :dataReferencia
        order by e.dataPrevistaDevolucao asc
    """)
    List<Emprestimo> findAtrasados(
            @Param("situacao")SituacaoEmprestimo situacao,
            @Param("dataReferencia")LocalDate dataReferencia
            );

    @Query("""
    select e
    from Emprestimo e
        where e.usuario.id = :usuarioId
        and e.situacao = :situacao
        and e.dataPrevistaDevolucao < :dataReferencia
            order by e.dataPrevistaDevolucao asc
    """)
    List<Emprestimo> findEmprestimosAtrasadosPorUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("situacao") SituacaoEmprestimo situacao,
            @Param("dataReferencia")LocalDate dataReferencia
            );
}