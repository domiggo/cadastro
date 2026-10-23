package com.example.cadastro.repository;

import com.example.cadastro.entities.PagamentoMulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;


public interface PagamentoMultaRepository extends JpaRepository<PagamentoMulta, Long> {

    List<PagamentoMulta> findByEmprestimoIdOrderByDataPagamentoAsc(Long emprestimoId);
    List<PagamentoMulta> findByEmprestimoUsuarioIdOrderByDataPagamentoAsc(Long usuarioId);


    @Query("""
        select coalesce(sum(p.valor), 0)
        from PagamentoMulta p
        where p.emprestimo.id = :emprestimoId
        """)
    BigDecimal somarPorEmprestimoId(@Param("emprestimoId") Long emprestimoId);
}
