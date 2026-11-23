package com.example.cadastro.repository;


import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.SituacaoEmprestimo;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    List<Emprestimo> findAll();

    Optional<Emprestimo> findByExemplarIdAndSituacao(Long exemplarId, SituacaoEmprestimo situacao);

    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    List<Emprestimo> findBySituacao(SituacaoEmprestimo situacao);

    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    Page<Emprestimo> findBySituacaoAndDataPrevistaDevolucaoBefore(
            SituacaoEmprestimo situacao,
            LocalDate dataReferencia,
            Pageable pageable
    );

    long countBySituacao(SituacaoEmprestimo situacao);

    long countBySituacaoAndDataPrevistaDevolucaoBefore(
            SituacaoEmprestimo situacao,
            LocalDate dataReferencia
    );

    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    List<Emprestimo> findByUsuarioId(Long usuarioId);

    @Query("""
            select e.dataPrevistaDevolucao as dataPrevistaDevolucao,
                   e.dataDevolucao as dataDevolucao,
                   e.dataEncerramento as dataEncerramento
            from Emprestimo e
            where e.usuario.id = :usuarioId
            """)
    List<EmprestimoDatasMulta> buscarDatasMultaPorUsuario(
            @Param("usuarioId") Long usuarioId
    );

    @Override
    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    Page<Emprestimo> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    Page<Emprestimo> findByUsuarioId(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    Page<Emprestimo> findBySituacao(SituacaoEmprestimo situacao, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    Page<Emprestimo> findByUsuarioIdAndSituacao(
            Long usuarioId,
            SituacaoEmprestimo situacao,
            Pageable pageable
    );

    @Query("""
            select e
            from Emprestimo e
            join fetch e.usuario
            join fetch e.exemplar exemplar
            join fetch exemplar.livro
            where e.usuario.id = :usuarioId
            """)
    List<Emprestimo> buscarComDadosParaMultasPorUsuario(
            @Param("usuarioId") Long usuarioId
    );

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndSituacao(Long usuarioId, SituacaoEmprestimo situacao);

    boolean existsByExemplarIdAndSituacao(Long exemplarId, SituacaoEmprestimo situacao);

    long countByExemplarIdAndSituacao(Long exemplarId, SituacaoEmprestimo situacao);

    @Query("""
                select case when count(e) > 0 then true else false end
            from Emprestimo e
            where e.exemplar.livro.id = :livroId
            """)
    boolean existsByLivroId(@Param("livroId") Long livroId);

    @Query("""
            select e
            from Emprestimo e
            where e.situacao = :situacao
              and e.dataPrevistaDevolucao < :dataReferencia
                order by e.dataPrevistaDevolucao asc
            """)
    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    List<Emprestimo> findAtrasados(
            @Param("situacao") SituacaoEmprestimo situacao,
            @Param("dataReferencia") LocalDate dataReferencia
    );

    @Query("""
            select e
            from Emprestimo e
                where e.usuario.id = :usuarioId
                and e.situacao = :situacao
                and e.dataPrevistaDevolucao < :dataReferencia
                    order by e.dataPrevistaDevolucao asc
            """)
    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    List<Emprestimo> findEmprestimosAtrasadosPorUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("situacao") SituacaoEmprestimo situacao,
            @Param("dataReferencia") LocalDate dataReferencia
    );

    @EntityGraph(attributePaths = {"usuario", "exemplar", "exemplar.livro"})
    Page<Emprestimo> findByUsuarioIdAndSituacaoAndDataPrevistaDevolucaoBefore(
            Long usuarioId,
            SituacaoEmprestimo situacao,
            LocalDate dataReferencia,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("Select e from Emprestimo e where e.id = :id")
    Optional<Emprestimo> buscarPorIdComBloqueio(@Param("id") Long id);
}
