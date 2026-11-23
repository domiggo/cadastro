package com.example.cadastro.repository;

import com.example.cadastro.dto.PagamentoMultaResumo;
import com.example.cadastro.entities.PagamentoMulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;


public interface PagamentoMultaRepository extends JpaRepository<PagamentoMulta, Long> {

    List<PagamentoMulta> findByEmprestimoIdOrderByDataPagamentoAsc(Long emprestimoId);

    List<PagamentoMulta> findByEmprestimoUsuarioIdOrderByDataPagamentoAsc(Long usuarioId);

    @Query(
            value = """
                    select new com.example.cadastro.dto.PagamentoMultaResumo(
                        p.id,
                        p.valor,
                        p.dataPagamento,
                        p.emprestimo.id
                    )
                    from PagamentoMulta p
                    where p.emprestimo.usuario.id = :usuarioId
                    """,
            countQuery = """
                    select count(p)
                    from PagamentoMulta p
                    where p.emprestimo.usuario.id = :usuarioId
                    """
    )
    Page<PagamentoMultaResumo> buscarResumoPorUsuario(
            @Param("usuarioId") Long usuarioId,
            Pageable pageable
    );


    @Query("""
            select coalesce(sum(p.valor), 0)
            from PagamentoMulta p
            where p.emprestimo.id = :emprestimoId
            """)
    BigDecimal somarPorEmprestimoId(@Param("emprestimoId") Long emprestimoId);

    @Query("""
            select p.emprestimo.id as emprestimoId, sum(p.valor) as totalPago
            from PagamentoMulta p
            where p.emprestimo.usuario.id = :usuarioId
            group by p.emprestimo.id
            """)
    List<TotalPagoPorEmprestimo> somarPorUsuarioAgrupadoPorEmprestimo(
            @Param("usuarioId") Long usuarioId
    );

}
