package com.example.cadastro.repository;


import com.example.cadastro.emprestimos.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    Optional<Emprestimo> findByLivroIdAndDataDevolucaoIsNull(Long livroId);

    List<Emprestimo> findByDataDevolucaoIsNull();
    List<Emprestimo> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
    boolean existsByLivroId(Long livroId);
    boolean existsByUsuarioIdAndDataDevolucaoIsNull(Long usuarioId);
}
